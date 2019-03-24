package com.codebot.axel.codex

import java.io.Serializable

/**
 * Created by Axel on 6/9/2018.
 */

// CodeX-Kernel data
class CodexInfo(val name: String, val description: String, val features: ArrayList<String>, val governors: ArrayList<String>, val schedulers: ArrayList<String>, val downloads: Downloads, val changelog: Array<Changelog>) : Serializable

class Downloads(val name: String, val ver: String, var url: String) : Serializable
class Changelog(val added: Array<String>) : Serializable