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

package com.msemu.core.network.packets.outpacket.wvscontext;

import com.msemu.commons.enums.OutHeader;
import com.msemu.commons.network.packets.OutPacket;
import com.msemu.core.network.GameClient;

/**
 * Created by Weber on 2018/4/23.
 */
public class LP_RequestEventList extends OutPacket<GameClient> {

    public LP_RequestEventList(boolean entry) {
        super(OutHeader.LP_RequestEventList);
        encodeInt(0); // nDefaultLevelLimit
        encodeByte(entry);

        encodeString("楓之谷活動"); //eventName
        encodeByte(false);
        encodeInt(0);
        encodeInt(1); // event Size
        // for loop
        for (int i = 0; i < 1; i++) {
            encodeInt(0x15B);
            encodeString("新增玩家活動");
            encodeString("");
            encodeInt(9);
            encodeInt(9);
            encodeInt(20180101); // event Start Time
            encodeInt(20181212); // event End Time
            encodeInt(0);
            encodeInt(1);
            encodeByte(0);
            encodeByte(0);
            encodeByte(0);
            encodeByte(0);
            encodeByte(1);
            int[] items = new int[]{2433956, 2433958, 2435301, 4001871, 2450064};
            encodeInt(items.length);
            for (int item : items) encodeInt(item);
            encodeInt(0);
            encodeByte(0);
            encodeByte(0);
        }
        // endloop
        encodeInt(0); // unk
    }

}
