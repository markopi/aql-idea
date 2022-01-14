package com.github.markopi.ideafirstplugin.thinkehr

// we cannot use them in constructor, since we do not want to add a dependency to jackson datatype kotlin just for this
class ThinkEhrQueryResponse {
    lateinit var q: String
    lateinit var columns: List<Column>
    lateinit var rows: List<Any?>

    class Column() {
        lateinit var name: String
        lateinit var path: String
    }
}