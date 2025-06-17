package com.pcoundia.jpa.dialect;

import org.hibernate.dialect.MySQL8Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CustomMysqlDialect extends MySQL8Dialect {

    public CustomMysqlDialect() {
        super();
        // Register the JSON_EXTRACT function
        registerFunction("json_extract", new StandardSQLFunction("json_extract", StandardBasicTypes.STRING));
        // Register the JSON_UNQUOTE function
        registerFunction("json_unquote", new StandardSQLFunction("json_unquote", StandardBasicTypes.STRING));
        // Register the TIMESTAMPDIFF function
        registerFunction("TIMESTAMPDIFF", new StandardSQLFunction("TIMESTAMPDIFF", StandardBasicTypes.INTEGER));


    }
}