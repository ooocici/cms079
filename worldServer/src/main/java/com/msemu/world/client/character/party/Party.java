/*
 * MIT License
 *
 * Copyright (c) 2018 msemu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.msemu.world.client.character.party;


import com.msemu.commons.network.packets.OutPacket;
import com.msemu.core.network.GameClient;
import com.msemu.core.network.packets.outpacket.wvscontext.LP_PartyResult;
import com.msemu.world.client.character.Character;
import com.msemu.world.client.character.party.operations.*;
import com.msemu.world.client.field.Field;
import com.msemu.world.data.FieldData;
import com.msemu.world.service.PartyService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Created by Weber on 2018/4/13.
 */
@Getter
@Setter
public class Party {

    private static final AtomicInteger partyIdGenerator = new AtomicInteger(1);

    private final int id;

    @Getter(value = AccessLevel.PRIVATE)
    private final PartyMember[] partyMembers = new PartyMember[6];

    private boolean appliable;

    private String name;

    private int partyLeaderId;

    @Getter(value = AccessLevel.PRIVATE)
    private Map<Integer, Field> fields = new HashMap<>();
    @Getter(value = AccessLevel.PRIVATE)
    private ReentrantLock lock = new ReentrantLock();


    public Party() {
        id = getNewPartyId();
    }

    public boolean isFull() {
        return Arrays.stream(getPartyMembers()).noneMatch(Objects::isNull);
    }

    public boolean isEmpty() {
        return Arrays.stream(getPartyMembers()).allMatch(Objects::isNull);
    }


    public void addPartyMember(Character chr) {
        boolean empty = isEmpty();
        PartyMember pm = new PartyMember(chr);
        getLock().lock();
        try {
            for (int i = 0; i < partyMembers.length; i++) {
                if (partyMembers[i] == null) {
                    partyMembers[i] = pm;
                    chr.setParty(this);
                    break;
                }
            }
        } finally {
            getLock().unlock();
        }
        if (empty) {
            changeLeader(pm, false);
            PartyService.getInstance().registerParty(this);
            chr.write(new LP_PartyResult(new CreateNewPartyDoneResponse(this)));
        }
        JoinPartyDoneResponse pjr = new JoinPartyDoneResponse(this, chr.getName());
        getOnlineMembers().forEach(partyMember -> partyMember.getCharacter().write(new LP_PartyResult(pjr)));
        updateFull();
    }

    public void updatePartyMemberInfo(Character chr) {
        getLock().lock();
        try {
            for (int i = 0; i < partyMembers.length; i++) {
                if (partyMembers[i] != null && partyMembers[i].getCharacterID() == chr.getId()) {
                    partyMembers[i] = new PartyMember(chr);
                    chr.setParty(this);
                }
            }
        } finally {
            getLock().unlock();
        }
        updateFull();
    }


    public void disbandParty() {
        List<PartyMember> members = getOnlineMembers();
        PartyMember leader = getPartyLeader();
        getLock().lock();
        try {
            for (int i = 0; i < partyMembers.length; i++) {
                if (partyMembers[i] != null) {
                    partyMembers[i].getCharacter().setParty(null);
                    partyMembers[i] = null;
                }
            }
        } finally {
            getLock().unlock();
        }
        WithdrawPartyDoneResponse wpr = new WithdrawPartyDoneResponse(this, leader, true, false);
        members.forEach(partyMember -> partyMember.getCharacter().write(new LP_PartyResult(wpr)));
        PartyService.getInstance().unregisterParty(this);
    }

    private int getNewPartyId() {
        return partyIdGenerator.getAndIncrement();
    }


    public List<PartyMember> getOnlineMembers() {
        return Arrays.stream(getPartyMembers()).filter(pm -> pm != null && pm.isOnline()).collect(Collectors.toList());
    }

    public List<PartyMember> getAllMembers() {
        return Arrays.stream(getPartyMembers()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void updateFull() {
        if (isEmpty()) {
            disbandParty();
        } else if (!getPartyLeader().isOnline()) {
            List<PartyMember> onlineMembers = getOnlineMembers();
            if (onlineMembers.size() > 0)
                changeLeader(onlineMembers.get(0), true);
        }
        LoadPartyDoneResponse upr = new LoadPartyDoneResponse(this);
        OutPacket<GameClient> outPacket = new LP_PartyResult(upr);
        for (PartyMember pm : getOnlineMembers()) {
            pm.getCharacter().write(outPacket);
        }
    }

    public void encode(OutPacket<GameClient> outPacket) {
        encode(outPacket, false);
    }

    public void encode(OutPacket<GameClient> outPacket, boolean isLeaving) {
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null ? pm.getCharacterID() : 0);
        }
        for (PartyMember pm : partyMembers) {
            outPacket.encodeString(pm != null ? pm.getCharacterName() : "", 15);
        }
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null ? pm.getCharacterJob() : 0);
        }
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null ? pm.getCharacterSubJob() : 0);
        }
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null ? pm.getCharacterLevel() : 0);
        }
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null && pm.isOnline() ? pm.getChannel() : -2);
        }
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null && pm.isOnline() ? pm.getChannel() : -2);
        }
        outPacket.encodeZeroBytes(6 * 4);
        outPacket.encodeInt(getPartyLeaderId());
        for (PartyMember pm : partyMembers) {
            outPacket.encodeInt(pm != null ? pm.getFieldID() : 0);
        }
        final TownPortal emptyTownPortal = new TownPortal();
        for (PartyMember pm : partyMembers) {
            if (pm != null && pm.getTownPortal() != null) {
                pm.getTownPortal().encodeForTown(outPacket, isLeaving);
            } else {
                emptyTownPortal.encodeForTown(outPacket, isLeaving);
            }
        }
        outPacket.encodeByte(isAppliable() && isFull());
        outPacket.encodeByte(false);
        outPacket.encodeString(getName());
        //
        outPacket.encodeByte(false);
        outPacket.encodeByte(false);
        int unkSize = 0;
        outPacket.encodeByte(unkSize);
        for (int i = 0; i < unkSize; i++) {
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
            outPacket.encodeString("");
            outPacket.encodeInt(0);
        }
        for (int i = 0; i < 3; i++) {
            outPacket.encodeByte(0);
            outPacket.encodeByte(0);
            outPacket.encodeByte(0);
            outPacket.encodeByte(0);
            outPacket.encodeInt(0);
            outPacket.encodeLong(0);
            outPacket.encodeInt(0);
            outPacket.encodeInt(0);
        }
    }

    public PartyMember getPartyLeader() {
        return Arrays.stream(getPartyMembers()).filter(p -> p != null && p.getCharacterID() == getPartyLeaderId()).findFirst().orElse(null);
    }

    public PartyMember getMemberById(int memberId) {
        return getAllMembers().stream().filter(partyMember -> partyMember.getCharacterID() == memberId)
                .findFirst().orElse(null);
    }

    private void removePartyMember(PartyMember memberToKick) {
        getLock().lock();
        try {
            for (int i = 0; i < partyMembers.length; i++) {
                if (partyMembers[i] != null && partyMembers[i] == memberToKick) {
                    partyMembers[i].getCharacter().setParty(null);
                    partyMembers[i] = null;
                    break;
                }
            }
        } finally {
            getLock().unlock();
        }
    }


    public void changeLeader(PartyMember newLeader, boolean dc) {
        getLock().lock();
        try {
            final int oldLeaderId = getPartyLeaderId();
            setPartyLeaderId(newLeader.getCharacterID());
            for (PartyMember partyMember : partyMembers) {
                if (partyMember != null) {
                    partyMember.setPartyLeaderId(newLeader.getCharacterID());
                }
            }
            PartyService.getInstance().updatePartyLeader(oldLeaderId, this);
            broadcast(new LP_PartyResult(new ChangePartyBossDoneResponse(getPartyLeaderId(), dc)));
        } finally {
            getLock().unlock();
        }
        updateFull();
    }

    public Field getOrCreateFieldById(int fieldID) {
        if (getFields().containsKey(fieldID)) {
            return getFields().get(fieldID);
        } else {
            Field field = FieldData.getInstance().getFieldFromTemplate(fieldID);
            getFields().put(field.getFieldId(), field);
            return field;
        }
    }

    public void withdrawParty(PartyMember member) {
        broadcast(new LP_PartyResult(new WithdrawPartyDoneResponse(this, member, false, false)));
        removePartyMember(member);
        updateFull();
    }

    public void kickPartyMember(PartyMember expelled) {
        removePartyMember(expelled);
        broadcast(new LP_PartyResult(new WithdrawPartyDoneResponse(this, expelled, false, true)));
        updateFull();
    }

    public void broadcast(OutPacket<GameClient> outPacket) {
        getOnlineMembers().forEach(partyMember -> {
            partyMember.getCharacter().write(outPacket);
        });
    }

    public void broadcast(OutPacket<GameClient> outPacket, Character chr) {
        getOnlineMembers().stream().filter(partyMember -> partyMember.getCharacterID() != chr.getId()).forEach(partyMember -> {
            partyMember.getCharacter().write(outPacket);
        });
    }


    public void changeSetting(boolean appliable, String partyName) {
        this.name = partyName;
        this.appliable = appliable;
        broadcast(new LP_PartyResult(new PartySettingDoneResponse(appliable, partyName)));
    }
}
