package org.example

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter

class PreProcessor {

    fun extractRefs(
        inputDir: String,
        outputFile: String,
        fileLimit: Int = 1,
        retryPath: String = "retry_preprocess.json"
    ) {

        // Read all jsonl files from directory and limit number of files to process
        val directoryPath = inputDir
        val numberOfFilesToProcess = fileLimit
        val allFiles = File(directoryPath).listFiles { _, name ->
            name.endsWith(".jsonl")
        }?.toList() ?: emptyList()
        val filesToProcess = allFiles.take(numberOfFilesToProcess)

        // Gson object for parsing JSON
        val gson = Gson()
        val retryList = mutableListOf<String>()

        // Track progress
        var fileCount = 0.0
        val numOfFiles = filesToProcess.size

        // File writer for the output file
        val outputWriter = FileWriter(outputFile, true) // Appending to file

        filesToProcess.forEach { file ->
            println(fileCount / numOfFiles)
            try {
                file.useLines { lines ->
                    lines.forEach { line ->
                        try {
                            // Parse each line as a JsonObject
                            val jsonObject = gson.fromJson(line, JsonObject::class.java)
                            val refs = jsonObject.get("bib_entries")?.asJsonObject
                            val paperId = gson.fromJson(Json.encodeToString(mapOf("paper_id" to jsonObject.get("paper_id")?.asString)), JsonObject::class.java).get("paper_id")
                            val dis = gson.fromJson(Json.encodeToString(mapOf("discipline" to jsonObject.get("discipline")?.asString)), JsonObject::class.java).get("discipline")
                            val metadata = jsonObject.get("metadata")?.asJsonObject

                            if (refs != null && dis != null && metadata != null) {
                                // Create a new JsonObject with the file path included
                                val outputJsonObject = JsonObject()
                                outputJsonObject.add("paper_id", paperId)
                                outputJsonObject.add("bib_entries", refs)
                                outputJsonObject.add("discipline", dis)
                                outputJsonObject.add("metadata", metadata)

                                // Write the JSON object as a new line in the JSONL output file
                                outputWriter.write(gson.toJson(outputJsonObject) + "\n")
                            } else {
                                println("Some fields are missing or not in the expected format")
                            }
                        } catch (e: Exception) {
                            println("Error parsing line in file ${file.path}: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error reading file: ${e.message}")
                retryList.add(file.path)
                println("File name saved in ${retryPath}, continuing")
            }
            fileCount++
        }

        // Close the writer after all files are processed
        outputWriter.close()

        // If there are files to retry, save them to retryPath
        if (retryList.isNotEmpty()) {
            exportAsJson(retryList, retryPath)
        }

        println("Processing complete and saved to $outputFile")
    }

    private fun exportAsJson(data: Any, filePath: String) {
        val gson = Gson()
        val file = File(filePath)
        file.writeText(gson.toJson(data))
    }
}
