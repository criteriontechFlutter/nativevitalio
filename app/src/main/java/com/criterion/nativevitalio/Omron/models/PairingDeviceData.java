package com.criterion.nativevitalio.Omron.models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.criterion.nativevitalio.Omron.Database.OmronDBConstans;
import com.criterion.nativevitalio.Omron.utility.Constants;
import com.criterion.nativevitalio.utils.MyApplication;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.SharedManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PairingDeviceData {
    private static List<Map<String, String>> pairingDeviceList;
    private static ContentResolver contentResolver;
    public PairingDeviceData(ContentResolver contentResolver){
        this.contentResolver = contentResolver;
        if(this.pairingDeviceList == null) {
            loadPairingDeviceInfoList();
        }
    }

    public void resetPairingDeviceList(){
        pairingDeviceList = new ArrayList<>();
    }

    public List<Map<String, String>> getPairingDeviceList(){
        return pairingDeviceList;
    }

    public void loadPairingDeviceInfoList() {
        pairingDeviceList = new ArrayList<>();
        Cursor curs = contentResolver.query(OmronDBConstans.PAIRING_DATA_CONTENT_URI, null, null, null, OmronDBConstans.PAIRING_DATA_INDEX + " DESC");
        if (curs != null) {
            int localNameIndex = curs.getColumnIndex(OmronDBConstans.PAIRING_DATA_LocalName);
            int uuidIndex = curs.getColumnIndex(OmronDBConstans.PAIRING_DATA_uuid);
            int userIndex = curs.getColumnIndex(OmronDBConstans.PAIRING_DATA_UserNumber);
            int sequenceNoIndex = curs.getColumnIndex(OmronDBConstans.PAIRING_DATA_SequenceNo);
            while (curs.moveToNext()) {
                String _localName = curs.getString(localNameIndex);
                String _uuid = curs.getString(uuidIndex);
                String _selectUser = curs.getString(userIndex);
                String _sequenceNo = curs.getString(sequenceNoIndex);
                OmronPeripheral peripheral = new OmronPeripheral(_localName, _uuid);
                addPairingDataToList(peripheral,_selectUser,_sequenceNo,true);
            }
            curs.close();
        }
    }

    public static void addPairingDataToDB(final OmronPeripheral peripheral,String sequenceNo) {
        List<Integer> user = peripheral.getSelectedUsers();
        addPairingDataToList(peripheral, String.valueOf(user.get(0)), sequenceNo,false);
        String localName = peripheral.getLocalName();
        String[] localNames = new String[]{localName};
        Cursor curs = contentResolver.query(OmronDBConstans.PAIRING_DATA_CONTENT_URI, null, OmronDBConstans.PAIRING_DATA_LocalName + " COLLATE NOCASE=?", localNames, OmronDBConstans.PAIRING_DATA_INDEX + " DESC");
        if(curs != null && curs.getCount() > 0) {//delete if it already exists
            int localNameIndex = curs.getColumnIndex(OmronDBConstans.PAIRING_DATA_LocalName);
            if (curs.moveToFirst()) { // Move the Cursor position
                localName = curs.getString(localNameIndex);
                contentResolver.delete(OmronDBConstans.PAIRING_DATA_CONTENT_URI, OmronDBConstans.PAIRING_DATA_LocalName + " COLLATE NOCASE=?", localNames);
            }
        }
        ContentValues cv = new ContentValues();
        cv.put(OmronDBConstans.PAIRING_DATA_UserNumber, user.get(0));
        cv.put(OmronDBConstans.PAIRING_DATA_SequenceNo, sequenceNo);
        cv.put(OmronDBConstans.PAIRING_DATA_uuid, peripheral.getUuid());
        cv.put(OmronDBConstans.PAIRING_DATA_LocalName, localName);
        contentResolver.insert(OmronDBConstans.PAIRING_DATA_CONTENT_URI, cv);
        if (curs != null) {
            curs.close();
        }
    }
    public void deleteDevice(Map<String, String> configInfo){
        String[] localNames = new String[]{configInfo.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME)};
        contentResolver.delete(OmronDBConstans.PAIRING_DATA_CONTENT_URI, OmronDBConstans.PAIRING_DATA_LocalName + " COLLATE NOCASE=?", localNames);
        removeItemFromPairingDeviceList(configInfo);
    }
    public void deleteAllDevices(){
        contentResolver.delete(OmronDBConstans.PAIRING_DATA_CONTENT_URI, null, null);
        resetPairingDeviceList();
    }
    public static boolean isDeviceAvailable(final OmronPeripheral peripheral){
        String localName = peripheral.getLocalName();
        for (Map<String, String> device : pairingDeviceList) {
            if (device.containsKey(Constants.deviceInfoKeys.KEY_LOCAL_NAME)
                    && device.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME).equals(localName)) {
                return true;
            }
        }
        return false;
    }

    public static OmronPeripheral changePeripheralObject(Map<String, String> configInfo){
        return new OmronPeripheral(configInfo.get(Constants.deviceInfoKeys.KEY_LOCAL_NAME), configInfo.get(Constants.deviceInfoKeys.KEY_UUID));
    }

    private static void addPairingDataToList(final OmronPeripheral peripheral,String selectUser, String sequenceNo ,boolean isInit){
        SharedManager configManager = OmronPeripheralManager.sharedManager(MyApplication.Companion.getAppContext());
        Map<String, String> configInfo = configManager.getConfiguration().getDeviceConfigGroupIdAndGroupIncludedId(peripheral.getDeviceGroupIDKey(), peripheral.getDeviceGroupIncludedGroupIDKey());
        if(configInfo != null) {
            configInfo.put(Constants.deviceInfoKeys.KEY_LOCAL_NAME, peripheral.getLocalName());
            configInfo.put(Constants.deviceInfoKeys.KEY_UUID, peripheral.getUuid());
            configInfo.put(Constants.deviceInfoKeys.KEY_SELECTED_USER, selectUser);
            configInfo.put(Constants.deviceInfoKeys.KEY_SEQUENCE_NO, sequenceNo);
            if (isInit) {
                pairingDeviceList.add(configInfo);
            } else {
                String key = Constants.deviceInfoKeys.KEY_LOCAL_NAME;
                String valueToAdd = configInfo.get(key).toLowerCase();
                boolean found = false;
                for (Map<String, String> item : pairingDeviceList) {
                    if (item.containsKey(key) && item.get(key).toLowerCase().equals(valueToAdd)) {
                        item.put(Constants.deviceInfoKeys.KEY_SEQUENCE_NO, sequenceNo);
                        pairingDeviceList.remove(item);
                        pairingDeviceList.add(0, item);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    pairingDeviceList.add(0, configInfo);
                }
            }
        }
    }
    private static void removeItemFromPairingDeviceList(List<Map<String, String>> item) {
        for(Map<String, String> device: item){
            removeItemFromPairingDeviceList(device);
        }
    }
    private static void removeItemFromPairingDeviceList(Map<String, String> item) {
        String key = Constants.deviceInfoKeys.KEY_LOCAL_NAME;
        String valueToRemove = item.get(key);

        for (Iterator<Map<String, String>> iterator = pairingDeviceList.iterator(); iterator.hasNext();) {
            Map<String, String> currentItem = iterator.next();
            if (currentItem.containsKey(key) && currentItem.get(key).equals(valueToRemove)) {
                iterator.remove();
            }
        }
    }
}
