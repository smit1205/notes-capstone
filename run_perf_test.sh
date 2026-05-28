#!/bin/bash
cd "/Users/mac air/Downloads/Capg/Capstone Project"
rm -rf results/html-report
rm -f results/summary_report.jtl
mkdir -p results

jmeter -n -t notes_api_performance_test.jmx -l results/summary_report.jtl

jmeter -g results/summary_report.jtl -o results/html-report

open results/html-report/index.html
