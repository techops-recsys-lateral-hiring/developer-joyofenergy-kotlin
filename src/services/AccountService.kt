package de.tw.energy.services

class AccountService(private val mappings: Map<String, String>) : Map<String, String> by mappings
