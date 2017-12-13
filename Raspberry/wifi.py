#!/usr/bin/env python

import socket
import os

cmdTemp = "cat /sys/class/thermal/thermal_zone0/temp"
cmdCpu = "vmstat 2 3 | tail -n1 | sed \"s/\ \ */\ /g\" | cut -d' ' -f 16"
cmdRam = "grep \"^MemFree:\" /proc/meminfo | cut -c19-24"
run = True
socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket.bind(('', 15016))
socket.listen(1)

while run:
        active = True
        client, address = socket.accept()
        print "{} connected".format( address )

        while active:

                response = client.recv(1024).decode()
                print response
                if response == "0":
                        print "Hello jsuis PI !"
                        client.send("Hello Android, Je suis PI!")
                        active = False
                if response == "1":
                        cpu = os.popen(cmdCpu, "r").read()
                        ram = os.popen(cmdRam, "r").read()
                        temp = os.popen(cmdTemp, "r").read() 
                        client.send(cpu+";"+ram+";"+temp)
                        active = False
                if response == "q":
                        active = False
                        run = False
                        print "Fermeture du programme"
                response = "-1"

        client.close()
        print("Socket Fermée !")
