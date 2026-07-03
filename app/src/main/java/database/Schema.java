package database;

public class Schema {

    public static class Reply {
        public static String name = "Reply";

        public static class Cols {
            public static String uuid = "uuid";
            public static String priority = "priority";
            public static String message = "message";
            public static String enabled = "enabled";
            public static String reply_unknown = "reply_unknown";
            public static String reply_to_list = "reply_to_list";
            public static String replace_equal_priority = "replace_equal_priority";
        }
    }

    public static class Contact {

        public static String name = "Contact";

        public static class Cols {
            public static String lookupKey = "lookup_key";
            public static String id = "contact_id";
            public static String name = "name";
            public static String phone = "phone";
            public static String active_reply = "active_reply";
            public static String replies = "replies";
            public static String color = "color";
            public static String secondary_color = "secondary_color";
        }

    }
}
