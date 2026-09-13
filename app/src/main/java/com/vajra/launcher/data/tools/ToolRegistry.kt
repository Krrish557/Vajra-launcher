package com.vajra.launcher.data.tools

import com.vajra.launcher.R
import com.vajra.launcher.models.InstallationState
import com.vajra.launcher.models.ToolAction
import com.vajra.launcher.models.ToolCategory
import com.vajra.launcher.models.ToolConfigOption
import com.vajra.launcher.models.ToolDefinition
import com.vajra.launcher.models.ToolDocumentation
import com.vajra.launcher.models.ToolEnvironment

object ToolRegistry {

    val categories: List<ToolCategory> = listOf(
        ToolCategory(
            id = "recon",
            title = "RECON",
            subtitle = "Information Gathering",
            iconRes = R.drawable.ic_cat_recon,
            accentColorRes = R.color.vajra_cat_recon
        ),
        ToolCategory(
            id = "network",
            title = "NETWORK",
            subtitle = "Analysis & Mapping",
            iconRes = R.drawable.ic_cat_network,
            accentColorRes = R.color.vajra_cat_network
        ),
        ToolCategory(
            id = "web",
            title = "WEB",
            subtitle = "Application Security",
            iconRes = R.drawable.ic_cat_web,
            accentColorRes = R.color.vajra_cat_web
        ),
        ToolCategory(
            id = "android",
            title = "ANDROID",
            subtitle = "Mobile Security",
            iconRes = R.drawable.ic_cat_android,
            accentColorRes = R.color.vajra_cat_android
        ),
        ToolCategory(
            id = "windows",
            title = "WINDOWS / AD",
            subtitle = "Post-Exploitation",
            iconRes = R.drawable.ic_cat_windows,
            accentColorRes = R.color.vajra_cat_windows
        ),
        ToolCategory(
            id = "reverse",
            title = "REVERSE",
            subtitle = "Engineering",
            iconRes = R.drawable.ic_cat_reverse,
            accentColorRes = R.color.vajra_cat_reverse
        ),
        ToolCategory(
            id = "password",
            title = "PASSWORD",
            subtitle = "Credential Tools",
            iconRes = R.drawable.ic_cat_password,
            accentColorRes = R.color.vajra_cat_password
        ),
        ToolCategory(
            id = "wireless",
            title = "WIRELESS",
            subtitle = "Wi-Fi & Bluetooth",
            iconRes = R.drawable.ic_cat_wireless,
            accentColorRes = R.color.vajra_cat_wireless
        ),
        ToolCategory(
            id = "osint",
            title = "OSINT",
            subtitle = "Open Source Intel",
            iconRes = R.drawable.ic_cat_osint,
            accentColorRes = R.color.vajra_cat_osint
        ),
        ToolCategory(
            id = "forensics",
            title = "FORENSICS",
            subtitle = "Analysis & Recovery",
            iconRes = R.drawable.ic_cat_forensics,
            accentColorRes = R.color.vajra_cat_forensics
        ),
        ToolCategory(
            id = "exploitation",
            title = "EXPLOITATION",
            subtitle = "Vulnerability Research",
            iconRes = R.drawable.ic_cat_exploit,
            accentColorRes = R.color.vajra_cat_exploitation
        ),
        ToolCategory(
            id = "scripts",
            title = "SCRIPTS",
            subtitle = "Automation",
            iconRes = R.drawable.ic_cat_scripts,
            accentColorRes = R.color.vajra_cat_scripts
        ),
        ToolCategory(
            id = "terminal",
            title = "TERMINAL",
            subtitle = "Interactive Shells",
            iconRes = R.drawable.ic_cat_terminal,
            accentColorRes = R.color.vajra_cat_terminal
        ),
        ToolCategory(
            id = "tools",
            title = "TOOLS",
            subtitle = "Utilities & Helpers",
            iconRes = R.drawable.ic_cat_tools,
            accentColorRes = R.color.vajra_cat_tools
        ),
        ToolCategory(
            id = "reporting",
            title = "REPORTING",
            subtitle = "Documentation & Logs",
            iconRes = R.drawable.ic_cat_reporting,
            accentColorRes = R.color.vajra_cat_reporting
        )
    )

    val tools: List<ToolDefinition> = listOf(
        // RECON Tools (matching reference list)
        ToolDefinition(
            id = "nmap",
            name = "Nmap",
            categoryId = "recon",
            description = "Network scanner",
            fullDescription = "Nmap is a powerful network scanning tool used to discover hosts, open ports, services, and more.",
            environment = ToolEnvironment.DEBIAN,
            executable = "nmap",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            version = "7.95",
            quickActions = listOf(
                ToolAction("quick_scan", "Quick Scan", "nmap -T4 -F {target}", "Fast scan top 100 ports"),
                ToolAction("ping_sweep", "Ping Sweep", "nmap -sn {target}", "ICMP and ARP host discovery")
            ),
            advancedOptions = listOf(
                ToolConfigOption("service_detection", "Service Version Detection", "-sV", defaultValue = true, "Probe open ports for service info"),
                ToolConfigOption("os_detection", "OS Fingerprinting", "-O", defaultValue = false, "Enable OS detection"),
                ToolConfigOption("aggressive_scan", "Aggressive Timing (-T4)", "-A", defaultValue = false, "Enable OS detection, version detection, script scanning, traceroute")
            ),
            documentation = ToolDocumentation(
                manPage = "nmap(1)",
                docsUrl = "https://nmap.org/book/man.html",
                syntax = "nmap [Scan Type...] [Options] {target specification}"
            ),
            examples = listOf(
                "nmap -sS 192.168.1.0/24",
                "nmap -A target.com",
                "nmap -p 1-1000 192.168.1.1"
            )
        ),
        ToolDefinition(
            id = "netdiscover",
            name = "Netdiscover",
            categoryId = "recon",
            description = "Active host discovery",
            fullDescription = "An active/passive address reconnaissance tool, mainly developed for wireless networks without DHCP servers.",
            executable = "netdiscover",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            version = "0.9",
            examples = listOf(
                "netdiscover -r 192.168.1.0/24",
                "netdiscover -p"
            )
        ),
        ToolDefinition(
            id = "subfinder",
            name = "Subfinder",
            categoryId = "recon",
            description = "Subdomain enumeration",
            fullDescription = "Subfinder is a subdomain discovery tool that discovers valid subdomains for websites by using passive online sources.",
            executable = "subfinder",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            version = "2.6.3",
            examples = listOf(
                "subfinder -d target.com",
                "subfinder -d target.com -o subs.txt"
            )
        ),
        ToolDefinition(
            id = "amass",
            name = "Amass",
            categoryId = "recon",
            description = "Attack surface mapping",
            fullDescription = "In-depth attack surface mapping and external asset discovery using open source information gathering and active recon techniques.",
            executable = "amass",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            examples = listOf(
                "amass enum -d target.com",
                "amass intel -whois -d target.com"
            )
        ),
        ToolDefinition(
            id = "whatweb",
            name = "WhatWeb",
            categoryId = "recon",
            description = "Web technology fingerprinting",
            fullDescription = "Next generation web scanner that identifies technologies used on websites including CMS, blogging platforms, and JS libraries.",
            executable = "whatweb",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            version = "0.5.5",
            examples = listOf(
                "whatweb target.com",
                "whatweb -v -a 3 target.com"
            )
        ),
        ToolDefinition(
            id = "theharvester",
            name = "theHarvester",
            categoryId = "recon",
            description = "Email, subdomains, hosts",
            fullDescription = "Gather emails, subdomains, hosts, employee names, open ports and banners from different public sources like search engines and PGP key servers.",
            executable = "theharvester",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            examples = listOf(
                "theHarvester -d target.com -b all"
            )
        ),
        ToolDefinition(
            id = "recon_ng",
            name = "Recon-ng",
            categoryId = "recon",
            description = "Reconnaissance framework",
            fullDescription = "A full-featured Web Reconnaissance framework written in Python with independent modules, database interaction, and built-in convenience functions.",
            executable = "recon-ng",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            version = "5.1.2",
            examples = listOf(
                "recon-ng",
                "marketplace install all"
            )
        ),
        ToolDefinition(
            id = "maltego_cli",
            name = "Maltego (CLI)",
            categoryId = "recon",
            description = "Link analysis",
            fullDescription = "CLI utility for extracting and analyzing open-source intelligence and transforming forensic data into actionable links.",
            executable = "maltego-cli",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_recon,
            examples = listOf(
                "maltego-cli --transform All -t target.com"
            )
        ),

        // NETWORK Tools
        ToolDefinition(
            id = "tcpdump",
            name = "tcpdump",
            categoryId = "network",
            description = "Packet analyzer",
            fullDescription = "A powerful command-line packet analyzer and libpcap capture utility.",
            executable = "tcpdump",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_network,
            version = "4.99.4",
            examples = listOf(
                "tcpdump -i wlan0 -nn",
                "tcpdump -i any port 80 -w http.pcap"
            )
        ),
        ToolDefinition(
            id = "tshark",
            name = "Tshark",
            categoryId = "network",
            description = "CLI Wireshark",
            fullDescription = "Terminal-based Wireshark network protocol analyzer.",
            executable = "tshark",
            state = InstallationState.AVAILABLE,
            iconRes = R.drawable.ic_cat_network,
            examples = listOf(
                "tshark -i wlan0",
                "tshark -r capture.pcap -Y 'http.request'"
            )
        ),
        ToolDefinition(
            id = "netcat",
            name = "Netcat",
            categoryId = "network",
            description = "TCP/IP Swiss Army knife",
            fullDescription = "Utility for reading from and writing to network connections using TCP or UDP.",
            executable = "nc",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_network,
            version = "1.10",
            examples = listOf(
                "nc -lvnp 4444",
                "nc target.com 80"
            )
        ),
        ToolDefinition(
            id = "socat",
            name = "Socat",
            categoryId = "network",
            description = "Multipurpose relay",
            fullDescription = "Relay for bidirectional data transfer between two independent data channels.",
            executable = "socat",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_network,
            examples = listOf(
                "socat TCP-LISTEN:8080,fork TCP:target.com:80"
            )
        ),
        ToolDefinition(
            id = "bettercap",
            name = "Bettercap",
            categoryId = "network",
            description = "Network attack & monitoring",
            fullDescription = "The Swiss Army knife for 802.11, BLE, IPv4 and IPv6 network reconnaissance and MITM attacks.",
            executable = "bettercap",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_network,
            examples = listOf(
                "bettercap -iface wlan0"
            )
        ),

        // WEB Tools
        ToolDefinition(
            id = "sqlmap",
            name = "SQLMap",
            categoryId = "web",
            description = "SQL injection scanner",
            fullDescription = "Automatic SQL injection and database takeover tool.",
            executable = "sqlmap",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_web,
            version = "1.8",
            examples = listOf(
                "sqlmap -u 'http://target.com/item?id=1' --batch",
                "sqlmap -u 'http://target.com/' --forms --dbs"
            )
        ),
        ToolDefinition(
            id = "gobuster",
            name = "Gobuster",
            categoryId = "web",
            description = "Directory/DNS bruteforcer",
            fullDescription = "Fast tool for brute-forcing URIs (directories and files) in web sites, DNS subdomains, and virtual hosts.",
            executable = "gobuster",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_web,
            version = "3.6.0",
            examples = listOf(
                "gobuster dir -u http://target.com -w /usr/share/wordlists/dirb/common.txt"
            )
        ),
        ToolDefinition(
            id = "ffuf",
            name = "FFUF",
            categoryId = "web",
            description = "Fast web fuzzer",
            fullDescription = "Fast web fuzzer written in Go for discovering endpoints, params, and virtual hosts.",
            executable = "ffuf",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_web,
            examples = listOf(
                "ffuf -u http://target.com/FUZZ -w wordlist.txt"
            )
        ),
        ToolDefinition(
            id = "nikto",
            name = "Nikto",
            categoryId = "web",
            description = "Web server scanner",
            fullDescription = "Comprehensive web server scanner for dangerous files, outdated versions, and specific server problems.",
            executable = "nikto",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_web,
            examples = listOf(
                "nikto -h http://target.com"
            )
        ),

        // ANDROID Tools
        ToolDefinition(
            id = "adb",
            name = "ADB",
            categoryId = "android",
            description = "Android Debug Bridge",
            fullDescription = "Versatile command-line tool that lets you communicate with an Android device instance.",
            executable = "adb",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_android,
            version = "35.0.0",
            examples = listOf(
                "adb devices",
                "adb shell getprop ro.build.version.release"
            )
        ),
        ToolDefinition(
            id = "apktool",
            name = "Apktool",
            categoryId = "android",
            description = "Reverse engineer APKs",
            fullDescription = "A tool for reverse engineering 3rd party, closed, binary Android apps into smali and resources.",
            executable = "apktool",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_android,
            version = "2.9.3",
            examples = listOf(
                "apktool d app.apk",
                "apktool b app_folder"
            )
        ),
        ToolDefinition(
            id = "jadx",
            name = "JADX",
            categoryId = "android",
            description = "DEX to Java decompiler",
            fullDescription = "Command line and GUI tools for producing Java source code from Android Dex and APK files.",
            executable = "jadx",
            state = InstallationState.AVAILABLE,
            iconRes = R.drawable.ic_cat_android,
            examples = listOf(
                "jadx -d out app.apk"
            )
        ),
        ToolDefinition(
            id = "frida",
            name = "Frida",
            categoryId = "android",
            description = "Dynamic instrumentation",
            fullDescription = "Dynamic instrumentation toolkit for developers, reverse-engineers, and security researchers.",
            executable = "frida",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_android,
            examples = listOf(
                "frida -U -f com.target.app -l script.js"
            )
        ),

        // WINDOWS / AD Tools
        ToolDefinition(
            id = "impacket",
            name = "Impacket",
            categoryId = "windows",
            description = "Network protocols collection",
            fullDescription = "Collection of Python classes for working with network protocols, focused on SMB, MSRPC, and Kerberos.",
            executable = "impacket",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_windows,
            version = "0.11.0",
            examples = listOf(
                "impacket-psexec domain/user:password@target.ip",
                "impacket-secretsdump domain/user:password@target.ip"
            )
        ),
        ToolDefinition(
            id = "netexec",
            name = "NetExec",
            categoryId = "windows",
            description = "Automating network pentesting",
            fullDescription = "The successor to CrackMapExec for automating security assessments of large Active Directory networks.",
            executable = "nxc",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_windows,
            examples = listOf(
                "nxc smb 192.168.1.0/24 -u user -p password"
            )
        ),

        // REVERSE Engineering Tools
        ToolDefinition(
            id = "radare2",
            name = "Radare2",
            categoryId = "reverse",
            description = "Disassembler & hex editor",
            fullDescription = "UNIX-like reverse engineering framework and command-line toolset.",
            executable = "r2",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_reverse,
            version = "5.8.8",
            examples = listOf(
                "r2 -d ./binary",
                "r2 -AA ./binary"
            )
        ),
        ToolDefinition(
            id = "strings",
            name = "Strings",
            categoryId = "reverse",
            description = "Extract printable characters",
            fullDescription = "Finds and prints text strings in binary files.",
            executable = "strings",
            state = InstallationState.INSTALLED,
            iconRes = R.drawable.ic_cat_reverse,
            examples = listOf(
                "strings -n 8 ./binary | grep -i pass"
            )
        ),
        ToolDefinition(
            id = "binwalk",
            name = "Binwalk",
            categoryId = "reverse",
            description = "Firmware analysis tool",
            fullDescription = "Fast, easy to use tool for analyzing, reverse engineering, and extracting firmware images.",
            executable = "binwalk",
            state = InstallationState.NOT_INSTALLED,
            iconRes = R.drawable.ic_cat_reverse,
            examples = listOf(
                "binwalk -e firmware.bin"
            )
        ),
        ToolDefinition(
            id = "termux",
            name = "Termux",
            categoryId = "terminal",
            description = "Terminal emulator & Linux environment",
            fullDescription = "Termux combines powerful terminal emulation with an extensive Linux package collection. It provides the core shell and execution subsystem for Vajra.",
            environment = ToolEnvironment.TERMUX,
            executable = "termux",
            state = InstallationState.AVAILABLE,
            iconRes = R.drawable.ic_cat_terminal,
            version = "0.118+",
            quickActions = listOf(
                ToolAction("open_termux", "Open Termux", "launch_app", "Launch external Termux terminal emulator")
            ),
            examples = listOf(
                "pkg update && pkg upgrade",
                "pkg install proot-distro",
                "proot-distro login debian"
            )
        )
    )

    fun getCategory(id: String): ToolCategory? =
        categories.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun getToolsForCategory(categoryId: String): List<ToolDefinition> =
        tools.filter { it.categoryId.equals(categoryId, ignoreCase = true) }

    fun getTool(id: String): ToolDefinition? =
        tools.firstOrNull { it.id.equals(id, ignoreCase = true) }
}
