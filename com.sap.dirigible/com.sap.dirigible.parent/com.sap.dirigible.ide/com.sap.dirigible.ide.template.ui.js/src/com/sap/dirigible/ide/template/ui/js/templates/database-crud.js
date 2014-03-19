var systemLib = require('system');
var ioLib = require('io');

// get method type
var method = request.getMethod();
method = method.toUpperCase();

//get primary keys (one primary key is supported!)
var idParameter = getPrimaryKey();

// retrieve the id as parameter if exist 
var id = xss.escapeSql(request.getParameter(idParameter));
var count = xss.escapeSql(request.getParameter('count'));
var metadata = xss.escapeSql(request.getParameter('metadata'));
var sort = xss.escapeSql(request.getParameter('sort'));
var limit = xss.escapeSql(request.getParameter('limit'));
var offset = xss.escapeSql(request.getParameter('offset'));
var desc = xss.escapeSql(request.getParameter('desc'));

if (limit === null) {
	limit = 100;
}
if (offset === null) {
	offset = 0;
}

if(!hasConflictingParameters()){
    // switch based on method type
    if ((method === 'POST')) {
        // create
        create${entityName}();
    } else if ((method === 'GET')) {
        // read
        if (id) {
            read${entityName}Entity(id);
        } else if (count !== null) {
            count${entityName}();
        } else if (metadata !== null) {
            metadata${entityName}();
        } else {
            read${entityName}List();
        }
    } else if ((method === 'PUT')) {
        // update
        update${entityName}();    
        
    } else if ((method === 'DELETE')) {
        // delete
        if(isInputParameterValid(idParameter)){
            delete${entityName}(id);
        }
        
    } else {
        makeError(javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST, 1, "Invalid HTTP Method");
    }    
}



// flush and close the response
response.getWriter().flush();
response.getWriter().close();

function hasConflictingParameters(){
    if(id !== null && count !== null){
        makeError(javax.servlet.http.HttpServletResponse.SC_EXPECTATION_FAILED, 1, "Precondition failed: conflicting parameters - id, count");
        return true;
    }
    if(id !== null && metadata !== null){
        makeError(javax.servlet.http.HttpServletResponse.SC_EXPECTATION_FAILED, 1, "Precondition failed: conflicting parameters - id, metadata");
        return true;
    }
    return false;
}

function isInputParameterValid(paramName){
    var param = request.getParameter(paramName);
    if(param === null || param === undefined){
        makeError(javax.servlet.http.HttpServletResponse.SC_PRECONDITION_FAILED, 1, "Expected parameter is missing: " + paramName);
        return false;
    }
    return true;
}

// print error
function makeError(httpCode, errCode, errMessage) {
    var body = {'err': {'code': errCode, 'message': errMessage}};
    response.setStatus(httpCode);
    response.setHeader("Content-Type", "application/json");
    response.getWriter().print(JSON.stringify(body));
}

// create entity by parsing JSON object from request body
function create${entityName}() {
    var input = ioLib.read(request.getReader());
    var message = JSON.parse(input);
    var connection = datasource.getConnection();
    try {
        var sql = "INSERT INTO ${tableName} (";
#foreach ($tableColumn in $tableColumns)
#if ($velocityCount > 1)
        sql += ",";
#end
        sql += "${tableColumn.getName()}";
#end
        sql += ") VALUES ("; 
#foreach ($tableColumn in $tableColumns)
#if ($velocityCount > 1)
        sql += ",";
#end
        sql += "?";
#end
        sql += ")";

        var statement = connection.prepareStatement(sql);
        var i = 0;
#foreach ($tableColumn in $tableColumns)
#if ($tableColumn.isKey())
        var id = db.getNext('${tableName}_${tableColumn.getName()}');
        statement.setInt(++i, id);
#else    
#if ($tableColumn.getType() == $INTEGER)
        statement.setInt(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $VARCHAR)
        statement.setString(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $CHAR)
        statement.setString(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $BIGINT)
        statement.setLong(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $SMALLINT)
        statement.setShort(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $FLOAT)
        statement.setFloat(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $DOUBLE)
        statement.setDouble(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $DATE)
        var js_date_${tableColumn.getName().toLowerCase()} =  new Date(Date.parse(message.${tableColumn.getName().toLowerCase()}));
        statement.setDate(++i, new java.sql.Date(js_date_${tableColumn.getName().toLowerCase()}.getTime() + js_date_${tableColumn.getName().toLowerCase()}.getTimezoneOffset()*60*1000));
#elseif ($tableColumn.getType() == $TIME)
        var js_date_${tableColumn.getName().toLowerCase()} =  new Date(Date.parse(message.${tableColumn.getName().toLowerCase()})); 
        statement.setTime(++i, new java.sql.Time(js_date_${tableColumn.getName().toLowerCase()}.getTime() + js_date_${tableColumn.getName().toLowerCase()}.getTimezoneOffset()*60*1000));
#elseif ($tableColumn.getType() == $TIMESTAMP)
        var js_date_${tableColumn.getName().toLowerCase()} =  new Date(Date.parse(message.${tableColumn.getName().toLowerCase()}));
        statement.setTimestamp(++i, new java.sql.Timestamp(js_date_${tableColumn.getName().toLowerCase()}.getTime() + js_date_${tableColumn.getName().toLowerCase()}.getTimezoneOffset()*60*1000));
#else
    // not supported type: message.${tableColumn.getName().toLowerCase()}
#end
#end
#end
        statement.executeUpdate();
        response.getWriter().println(id);
    } catch(e){
        var errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
        makeError(errorCode, errorCode, e.message);
    } finally {
        connection.close();
    }
}

// read single entity by id and print as JSON object to response
function read${entityName}Entity(id) {
    var connection = datasource.getConnection();
    try {
        var result = "";
        var sql = "SELECT * FROM ${tableName} WHERE "+pkToSQL();
        var statement = connection.prepareStatement(sql);
        statement.setString(1, id);
        
        var resultSet = statement.executeQuery();
        var value;
        while (resultSet.next()) {
            result = createEntity(resultSet);
        }
        if(result.length === 0){
            makeError(javax.servlet.http.HttpServletResponse.SC_NOT_FOUND, 1, "Record with id: " + id + " does not exist.");
        }
        var text = JSON.stringify(result, null, 2);
        response.getWriter().println(text);
    } catch(e){
        var errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
        makeError(errorCode, errorCode, e.message);
    } finally {
        connection.close();
    }
}

// read all entities and print them as JSON array to response
function read${entityName}List() {
    var connection = datasource.getConnection();
    try {
        var result = [];
        var sql = "SELECT ";
        if (limit !== null && offset !== null) {
            sql += " " + db.createTopAndStart(limit, offset);
        }
        sql += " * FROM ${tableName}";
        if (sort !== null) {
            sql += " ORDER BY " + sort;
        }
        if (sort !== null && desc !== null) {
            sql += " DESC ";
        }
        if (limit !== null && offset !== null) {
            sql += " " + db.createLimitAndOffset(limit, offset);
        }
        var statement = connection.prepareStatement(sql);
        var resultSet = statement.executeQuery();
        var value;
        while (resultSet.next()) {
            result.push(createEntity(resultSet));
        }
        var text = JSON.stringify(result, null, 2);
        response.getWriter().println(text);
    } catch(e){
        var errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
        makeError(errorCode, errorCode, e.message);
    } finally {
        connection.close();
    }
}

//create entity as JSON object from ResultSet current Row
function createEntity(resultSet, data) {
    var result = {};
#foreach ($tableColumn in $tableColumns)
#if ($tableColumn.getType() == $INTEGER)
	result.${tableColumn.getName().toLowerCase()} = resultSet.getInt("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $VARCHAR)
    result.${tableColumn.getName().toLowerCase()} = resultSet.getString("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $CHAR)
    result.${tableColumn.getName().toLowerCase()} = resultSet.getString("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $BIGINT)
    result.${tableColumn.getName().toLowerCase()} = resultSet.getLong("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $SMALLINT)
    result.${tableColumn.getName().toLowerCase()} = resultSet.getShort("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $FLOAT)
    result.${tableColumn.getName().toLowerCase()} = resultSet.getFloat("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $DOUBLE)
    result.${tableColumn.getName().toLowerCase()} = resultSet.getDouble("${tableColumn.getName()}");
#elseif ($tableColumn.getType() == $DATE) 
    result.${tableColumn.getName().toLowerCase()} = new Date(resultSet.getDate("${tableColumn.getName()}").getTime() - resultSet.getDate("${tableColumn.getName()}").getTimezoneOffset()*60*1000);
#elseif ($tableColumn.getType() == $TIME)
    result.${tableColumn.getName().toLowerCase()} = new Date(resultSet.getTime("${tableColumn.getName()}").getTime() - resultSet.getDate("${tableColumn.getName()}").getTimezoneOffset()*60*1000);
#elseif ($tableColumn.getType() == $TIMESTAMP)
    result.${tableColumn.getName().toLowerCase()} = new Date(resultSet.getTimestamp("${tableColumn.getName()}").getTime() - resultSet.getDate("${tableColumn.getName()}").getTimezoneOffset()*60*1000);
#else
    // not supported type: ${tableColumn.getName()}
#end
#end
    return result;
}

// update entity by id
function update${entityName}() {
    var input = ioLib.read(request.getReader());
    var message = JSON.parse(input);
    var connection = datasource.getConnection();
    try {
        var sql = "UPDATE ${tableName} SET ";
#foreach ($tableColumn in $tableColumnsWithoutKeys)
#if ($velocityCount > 1)
        sql += ",";
#end
        sql += "${tableColumn.getName()} = ?";
#end
#foreach ($tableColumn in $tableColumns)
#if ($tableColumn.isKey())
        sql += " WHERE ${tableColumn.getName()} = ?";
#end
#end
        var statement = connection.prepareStatement(sql);
        var i = 0;
#foreach ($tableColumn in $tableColumnsWithoutKeys)
#if ($tableColumn.getType() == $INTEGER)
        statement.setInt(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $VARCHAR)
        statement.setString(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $CHAR)
        statement.setString(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $BIGINT)
        statement.setLong(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $SMALLINT)
        statement.setShort(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $FLOAT)
        statement.setFloat(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $DOUBLE)
        statement.setDouble(++i, message.${tableColumn.getName().toLowerCase()});
#elseif ($tableColumn.getType() == $DATE)
        var js_date =  new Date(Date.parse(message.${tableColumn.getName().toLowerCase()}));
        statement.setDate(++i, new java.sql.Date(js_date_${tableColumn.getName().toLowerCase()}.getTime() + js_date_${tableColumn.getName().toLowerCase()}.getTimezoneOffset()*60*1000));
#elseif ($tableColumn.getType() == $TIME)
        var js_date_${tableColumn.getName().toLowerCase()} =  new Date(Date.parse(message.${tableColumn.getName().toLowerCase()})); 
        statement.setTime(++i, new java.sql.Time(js_date_${tableColumn.getName().toLowerCase()}.getTime() + js_date_${tableColumn.getName().toLowerCase()}.getTimezoneOffset()*60*1000));
#elseif ($tableColumn.getType() == $TIMESTAMP)
        var js_date_${tableColumn.getName().toLowerCase()} =  new Date(Date.parse(message.${tableColumn.getName().toLowerCase()}));
        statement.setTimestamp(++i, new java.sql.Timestamp(js_date_${tableColumn.getName().toLowerCase()}.getTime() + js_date_${tableColumn.getName().toLowerCase()}.getTimezoneOffset()*60*1000));
#else
    // not supported type: message.${tableColumn.getName().toLowerCase()}
#end
#end
        var id = "";
#foreach ($tableColumn in $tableColumns)
#if ($tableColumn.isKey())
        id = message.${tableColumn.getName().toLowerCase()};
        statement.setInt(++i, id);
#end
#end
        statement.executeUpdate();
        response.getWriter().println(id);
    } catch(e){
        var errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
        makeError(errorCode, errorCode, e.message);
    } finally {
        connection.close();
    }
}

// delete entity
function delete${entityName}(id) {
    var connection = datasource.getConnection();
    try {
        var sql = "DELETE FROM ${tableName} WHERE "+pkToSQL();
        var statement = connection.prepareStatement(sql);
        statement.setString(1, id);
        var resultSet = statement.executeUpdate();
        response.getWriter().println(id);
    } catch(e){
        var errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
        makeError(errorCode, errorCode, e.message);
    } finally {
        connection.close();
    }
}

function count${entityName}() {
    var count = 0;
    var connection = datasource.getConnection();
    try {
        var statement = connection.createStatement();
        var rs = statement.executeQuery('SELECT COUNT(*) FROM ${tableName}');
        while (rs.next()) {
            count = rs.getInt(1);
        }
    } catch(e){
        var errorCode = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
        makeError(errorCode, errorCode, e.message);
    } finally {
        connection.close();
    }
    response.getWriter().println(count);
}

function metadata${entityName}() {
	var entityMetadata = {};
	entityMetadata.name = '${tableName.toLowerCase()}';
	entityMetadata.type = 'object';
	entityMetadata.properties = [];
	
#foreach ($tableColumn in $tableColumns)
	var property${tableColumn.getName().toLowerCase()} = {};
	property${tableColumn.getName().toLowerCase()}.name = '$tableColumn.getName().toLowerCase()';
#if ($tableColumn.getType() == $INTEGER)
	property${tableColumn.getName().toLowerCase()}.type = 'integer';
#elseif ($tableColumn.getType() == $VARCHAR)
    property${tableColumn.getName().toLowerCase()}.type = 'string';
#elseif ($tableColumn.getType() == $CHAR)
	property${tableColumn.getName().toLowerCase()}.type = 'string';
#elseif ($tableColumn.getType() == $BIGINT)
	property${tableColumn.getName().toLowerCase()}.type = 'bigint';
#elseif ($tableColumn.getType() == $SMALLINT)
	property${tableColumn.getName().toLowerCase()}.type = 'smallint';
#elseif ($tableColumn.getType() == $FLOAT)
	property${tableColumn.getName().toLowerCase()}.type = 'float';
#elseif ($tableColumn.getType() == $DOUBLE)
    property${tableColumn.getName().toLowerCase()}.type = 'double';
#elseif ($tableColumn.getType() == $DATE)
    property${tableColumn.getName().toLowerCase()}.type = 'date';
#elseif ($tableColumn.getType() == $TIME)
    property${tableColumn.getName().toLowerCase()}.type = 'time';
#elseif ($tableColumn.getType() == $TIMESTAMP)
    property${tableColumn.getName().toLowerCase()}.type = 'timestamp';
#else
    property${tableColumn.getName().toLowerCase()}.type = 'unknown';
#end
#if ($tableColumn.isKey())
	property${tableColumn.getName().toLowerCase()}.key = 'true';
	property${tableColumn.getName().toLowerCase()}.required = 'true';
#end
    entityMetadata.properties.push(property${tableColumn.getName().toLowerCase()});

#end

    response.getWriter().println(JSON.stringify(entityMetadata));
}

function getPrimaryKeys(){
    var result = [];
    var i = 0;
#foreach ($tableColumn in $tableColumns)
#if ($tableColumn.isKey())
    result[i++] = '$tableColumn.getName()';
#end
#end
    if (result === 0) {
        throw new Exception("There is no primary key");
    } else if(result.length > 1) {
        throw new Exception("More than one Primary Key is not supported.");
    }
    return result;
}

function getPrimaryKey(){
	return getPrimaryKeys()[0].toLowerCase();
}

function pkToSQL(){
    var pks = getPrimaryKeys();
    return pks[0] + " = ?";
}