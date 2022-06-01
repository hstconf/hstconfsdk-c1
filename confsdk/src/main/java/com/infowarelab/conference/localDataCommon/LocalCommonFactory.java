package com.infowarelab.conference.localDataCommon;

public class LocalCommonFactory {
    private static LocalCommonFactory instance = null;

    private ContactDataCommon contactDataCommon;

    public static LocalCommonFactory getInstance() {
        if (instance == null) {
            instance = new LocalCommonFactory();
        }
        return instance;
    }

    public ContactDataCommon getContactDataCommon() {
        return contactDataCommon;
    }

    public void setContactDataCommon(ContactDataCommon contactDataCommon) {
        this.contactDataCommon = contactDataCommon;
    }

}
