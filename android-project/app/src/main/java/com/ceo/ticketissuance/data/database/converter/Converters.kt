package com.ceo.ticketissuance.data.database.converter

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, formatter) }
    }
    
    @TypeConverter
    fun fromBigDecimal(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.toString()
    }
    
    @TypeConverter
    fun toBigDecimal(bigDecimalString: String?): BigDecimal? {
        return bigDecimalString?.let { BigDecimal(it) }
    }
}