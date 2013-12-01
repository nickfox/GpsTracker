//
//  RmsHelper.java
//  GpsTracker
//
//  Created by Nick Fox on 11/7/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

package com.websmithing.gpstracker;

import java.util.*;
import javax.microedition.rms.*;

public class RmsHelper {
    private GpsTracker midlet;
    private String mRecordStoreName;
    private Hashtable mHashtable;

    public RmsHelper(GpsTracker Midlet, String recordStoreName) throws RecordStoreException {
        this.midlet = Midlet;
        this.mRecordStoreName = recordStoreName;
        this.mHashtable = new Hashtable();
        load();
    }

    public String get(String key) {
        return (String)mHashtable.get(key);
    }

    public void put(String key, String value) {
        if (value == null) value = "";
        mHashtable.put(key, value);
    }

    private void load() throws RecordStoreException {
        RecordStore rs = null;
        RecordEnumeration re = null;

        try {
            rs = RecordStore.openRecordStore(mRecordStoreName, true);
            re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                byte[] raw = re.nextRecord();
                String pref = new String(raw);
                // Parse out the name.
                int index = pref.indexOf('|');
                String name = pref.substring(0, index);
                String value = pref.substring(index + 1);
                put(name, value);
            }
        }
        finally {
            if (re != null) re.destroy();
            if (rs != null) rs.closeRecordStore();
        }
    }

    public void save() throws RecordStoreException {
        RecordStore rs = null;
        RecordEnumeration re = null;
        try {
            rs = RecordStore.openRecordStore(mRecordStoreName, true);
            re = rs.enumerateRecords(null, null, false);

            // First remove all records, a little clumsy.
            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                rs.deleteRecord(id);
            }

            // Now save the preferences records.
            Enumeration keys = mHashtable.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = get(key);
                String pref = key + "|" + value;
                byte[] raw = pref.getBytes();
                rs.addRecord(raw, 0, raw.length);
            }
        }
        finally {
            if (re != null) re.destroy();
            if (rs != null) rs.closeRecordStore();
        }
    }

}

