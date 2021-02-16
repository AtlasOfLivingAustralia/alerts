# ALA Alerts

This is a small app responsible for sending email alerts when there are changes detected to endpoint web services (subscriptions).

# Build status

[![Build Status](https://travis-ci.com/AtlasOfLivingAustralia/alerts.svg?branch=master)](https://travis-ci.com/AtlasOfLivingAustralia/alerts)

# Dev environment set up

1. Install MySql
1. Log in as root
1. ```create user 'alerts_user'@'localhost' identified as 'alerts_user';```
1. ```grant all privileges on *.* to 'alerts_user'@'localhost';```
1. ```create database alerts```
1. Create /data/alerts/config/alerts-config.properties
  1. Use the template in ala-install to get the necessary values
