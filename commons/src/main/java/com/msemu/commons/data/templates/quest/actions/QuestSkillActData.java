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

package com.msemu.commons.data.templates.quest.actions;

import com.msemu.commons.data.enums.QuestActDataType;
import com.msemu.commons.data.loader.dat.DatSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Weber on 2018/4/22.
 */
public class QuestSkillActData extends QuestActData {

    @Getter
    List<Short> jobs = new ArrayList<>();
    @Getter
    @Setter
    private int skill;
    @Getter
    @Setter
    private int level = 1;
    @Getter
    @Setter
    private int masterLevel = 1;

    @Override
    public QuestActDataType getType() {
        return QuestActDataType.skill;
    }

    @Override
    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(skill);
        dos.writeShort(level);
        dos.writeInt(jobs.size());
        for (Short v : jobs)
            dos.writeShort(v);
    }

    @Override
    public DatSerializable load(DataInputStream dis) throws IOException {
        setSkill(dis.readInt());
        setLevel(dis.readShort());
        int size = dis.readInt();
        for (int i = 0; i < size; i++)
            getJobs().add(dis.readShort());
        return this;
    }
}
