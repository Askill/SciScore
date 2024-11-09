package org.example

import com.google.gson.Gson
import java.io.File


fun exportAsJson(export: Any, outputFile: String){
    val gson = Gson()
    val jsonString = gson.toJson(export)
    val outputFile = File(outputFile)
    outputFile.writeText(jsonString)
}