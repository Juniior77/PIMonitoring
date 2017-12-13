#!/usr/bin/env python
import bluetooth
import os

cmdTemp = "cat /sys/class/thermal/thermal_zone0/temp"
cmdCpu = "vmstat 2 3 | tail -n1 | sed \"s/\ \ */\ /g\" | cut -d' ' -f 16"
cmdRam = "grep \"^MemFree:\" /proc/meminfo | cut -c19-24"
while 1:
    server_socket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
 
    port = 1
    server_socket.bind(("",port))
    server_socket.listen(1)
 
    client_socket,address = server_socket.accept()
    print ("Accepted connection from ",address)

    while 1:
        data = client_socket.recv(1024)
        print (data)
        #if (data == "0"):
        #    print ("Hello je suis un Android !")
        #    client_socket.send("Bonjour Android ! Je suis PI !")
        if (data == "1"):
            print ("Je suis Android je veux de la data !")
            cpu = os.popen(cmdCpu, "r").read()
            ram = os.popen(cmdRam, "r").read()
            temp = os.popen(cmdTemp, "r").read()
            client_socket.send(cpu+";"+ram+";"+temp)
        if (data == "q"):
            print ("Wouahhhh Quitte !")
            break
        data = "toto"
    client_socket.close()
    server_socket.close()
