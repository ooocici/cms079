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

package com.msemu.world.client.character.jobs.legend;

import com.msemu.commons.network.packets.InPacket;
import com.msemu.world.client.character.AttackInfo;
import com.msemu.world.client.character.Character;
import com.msemu.world.client.character.HitInfo;
import com.msemu.world.client.character.SkillUseInfo;
import com.msemu.world.client.character.jobs.JobHandler;
import com.msemu.world.client.character.stats.TemporaryStatManager;

import java.util.Arrays;

/**
 * Created by Weber on 2018/4/12.
 */
public class Evan extends JobHandler {

    // 龍魔導士一轉
    public static final int 魔力之環I = 22001010;
    public static final int 瞬間移動 = 22001011;
    public static final int 魔心防禦 = 22001012;
    public static final int 龍之魂 = 22000013;
    public static final int 魔幻鏈 = 22000014;
    public static final int 龍之火花 = 22000015;

    // 龍魔導士二轉
    public static final int 魔力之環II = 22110010;
    public static final int 風之環 = 22111011;
    public static final int 迅捷_回來吧 = 22110013;
    public static final int 風之捷 = 22110014;
    public static final int 智慧昇華 = 22110015;
    public static final int 交感 = 22110016;
    public static final int 咒語精通 = 22110018;
    public static final int 援助跳躍 = 22110019;
    public static final int 極速詠唱 = 22111020;
    public static final int 龍之捷 = 22110022;

    //三轉
    public static final int 魔力之環III = 22140010;
    public static final int 閃雷之環 = 22141011;
    public static final int 自然力重置 = 22141016;

    public static final int 潛水_回來吧 = 22140013; // 增加隊友攻擊速度1階段


    //四轉
    public static final int 氣息_回來吧 = 22170064; // 對敵人使用火焰屏障
    public static final int 楓葉祝福 = 22171000;
    public static final int 聖歐尼斯龍 = 22171081;
    public static final int 英雄歐尼斯 = 22171082;
    public static final int 龍之主 = 22171080;
    public static final int 龍之主2 = 22171083;
    public static final int 魔力之環IV = 22170060;
    public static final int 魔力之環IV_ADDED = 22170061;
    public static final int 地之環 = 22171062;
    public static final int 龍神之怒 = 22171095;


    private static final int[] buffs = {
            魔心防禦,
            極速詠唱,
            自然力重置,
            潛水_回來吧,
            楓葉祝福,
            英雄歐尼斯,
            聖歐尼斯龍,
            龍之主,
            龍之主2,
    };

    private static final int[] evanSkills = {
            魔力之環I,
            魔力之環II,
            魔力之環III,
            魔力之環IV,
            魔力之環IV_ADDED,
            風之環,
            閃雷之環,
            地之環,
            龍神之怒,
    };


    public Evan(Character character) {
        super(character);
    }

    @Override
    public boolean handleAttack(AttackInfo attackInfo) {

        Character chr = getCharacter();

        TemporaryStatManager tsm = chr.getTemporaryStatManager();

        return true;

    }

    @Override
    public boolean handleSkillUse(SkillUseInfo skillUseInfo) {
        return true;
    }

    @Override
    public boolean isHandlerOfJob(short id) {
        return false;
    }

    @Override
    public int getFinalAttackSkill() {
        return 0;
    }

    @Override
    public boolean isBuff(int skillID) {
        return Arrays.stream(buffs).anyMatch(b -> b == skillID);
    }

    @Override
    public void handleHitPacket(InPacket inPacket, HitInfo hitInfo) {

    }

    private boolean isEvanSkill(int skillID) {
        return Arrays.stream(evanSkills).anyMatch(b -> b == skillID);
    }
}
