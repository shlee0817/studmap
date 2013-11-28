package de.whs.studmap.client.core.web;

public interface ResponseError {
			public static final int None = 0;

            public static final int DatabaseError = 1;
            
            
            public static final int UserNameDuplicate = 101;
            public static final int UserNameInvalid = 102;
            public static final int PasswordInvalid = 103;
            
            public static final int LoginInvalid = 110;

            public static final int MapIdDoesNotExist = 201;
            public static final int FloorIdDoesNotExist = 202;
}
