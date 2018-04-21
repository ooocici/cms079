package com.msemu.world.client.character.quest.requirements;

import com.msemu.commons.data.dat.DatSerializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Weber on 2018/4/13.
 */
@Entity
@DiscriminatorValue("money")
public class QuestProgressMoneyRequirement extends QuestProgressRequirement {

    @Column(name = "requiredCount")
    private int money;
    @Column(name = "currentCount")
    private int curMoney;

    public QuestProgressMoneyRequirement() {
    }

    public QuestProgressMoneyRequirement(int money){
        this.money = money;
    }

    @Override
    public boolean isComplete() {
        return getCurMoney() >= getMoney();
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getCurMoney() {
        return curMoney;
    }

    public void setCurMoney(int curMoney) {
        this.curMoney = curMoney;
    }

    @Override
    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(getMoney());
    }

    @Override
    public DatSerializable load(DataInputStream dis) throws IOException {
        return new QuestProgressMoneyRequirement(dis.readInt());
    }

    public void addMoney(int money) {
        setMoney(getMoney() + money);
    }
}

