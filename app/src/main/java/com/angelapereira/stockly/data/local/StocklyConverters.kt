package com.angelapereira.stockly.data.local

import androidx.room.TypeConverter

class StocklyConverters {

    @TypeConverter
    fun fromMovementType(type: MovementType): String {
        return type.name
    }

    @TypeConverter
    fun toMovementType(value: String): MovementType {
        return MovementType.valueOf(value)
    }
}