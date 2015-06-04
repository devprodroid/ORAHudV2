package devprodroid.bluetooth;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public interface Event {
    byte MSG_TYPE_KEYBOARD = 0;
    byte MSG_TYPE_MOUSE_DOWN = 1;
    byte MSG_TYPE_MOUSE_UP = 2;
    byte MSG_TYPE_MOUSE_MOVE = 3;
    byte MSG_TYPE_BATT_LEVEL = 4;
    int KEYBOARD_MSG_PAYLOAD_LENGTH = 8;
    int MOUSE_DOWN_MSG_PAYLOAD_LENGTH = 0;
    int MOUSE_UP_MSG_PAYLOAD_LENGTH = 0;
    int MOUSE_MOVE_MSG_PAYLOAD_LENGTH = 8;
}

