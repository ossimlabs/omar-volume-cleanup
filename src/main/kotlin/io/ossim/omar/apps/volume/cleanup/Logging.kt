package io.ossim.omar.apps.volume.cleanup

import java.sql.ResultSet
import java.sql.SQLException
import java.util.*


/**
 * Returns the receiver in byte text format (e.g. 3.2 KiB)
 * @param si Use decimal prefixes (SI) (e.g. kB) instead of binary prefixes (IEC) (e.g. KiB)
 * https://stackoverflow.com/a/3758880/2832996
 */
internal fun Long.humanReadableByteCount(si: Boolean = false): String {
    val unit = if (si) 1000 else 1024
    if (this < unit) return "$this B"
    val exp = (Math.log(toDouble()) / Math.log(unit.toDouble())).toInt()
    val prefix = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    return String.format("%.1f %sB", this / Math.pow(unit.toDouble(), exp.toDouble()), prefix)
}

internal fun log(message: String) = println("[${Date()}] $message")

/**
 * Print a result set to system out.
 *
 * @param rs The ResultSet to print
 * @throws SQLException If there is a problem reading the ResultSet
 */
fun printResultSet(rs: ResultSet) {

    // Prepare metadata object and get the number of columns.
    val rsmd = rs.metaData
    val columnsNumber = rsmd.columnCount

    // Print column names (a header).
    for (i in 1..columnsNumber) {
        if (i > 1) print(" | ")
        print(rsmd.getColumnName(i))
    }
    println("")

    while (rs.next()) {
        for (i in 1..columnsNumber) {
            if (i > 1) print(" | ")
            print(rs.getString(i))
        }
        println("")
    }
}