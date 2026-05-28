#!/bin/bash

export GROQ_API_KEY=GROQ_API_KEY_PLACEHOLDER

mvn exec:java \
  -Dexec.mainClass="agentic.MCPCommandLineRunner" \
  -Dexec.classpathScope="test" \
  -Dcmd="$1"