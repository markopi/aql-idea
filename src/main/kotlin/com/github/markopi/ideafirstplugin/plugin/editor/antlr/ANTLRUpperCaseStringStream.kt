package com.github.markopi.ideafirstplugin.plugin.editor.antlr

import org.antlr.runtime.ANTLRStringStream

class ANTLRUpperCaseStringStream(aqlString: String) : ANTLRStringStream(aqlString) {
    override fun LA(i: Int): Int {
        var i = i
        if (i == 0) {
            return 0 // undefined
        }
        if (i < 0) {
            i++ // e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
            if (p + i - 1 < 0) {
                return EOF // invalid; no char before first char
            }
        }
        return if (p + i - 1 >= n) {
            //System.out.println("char LA("+i+")=EOF; p="+p);
            EOF
        } else Character.toUpperCase(data[p + i - 1]).toInt()
        //System.out.println("char LA("+i+")="+(char)data[p+i-1]+"; p="+p);
        //System.out.println("LA("+i+"); p="+p+" n="+n+" data.length="+data.length);
    }
}