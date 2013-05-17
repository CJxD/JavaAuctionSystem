#!/bin/bash
cd "$(dirname "$0")"
cd src
javac AuctionSystem.java
cd ../bin
java AuctionSystem