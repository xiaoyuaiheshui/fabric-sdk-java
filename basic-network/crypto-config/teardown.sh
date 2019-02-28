#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#
# Exit on first error, print all commands.
set -e

# Shut down the Docker containers for the system tests.
docker-compose -f docker-orderer.yml kill && docker-compose -f docker-orderer.yml down
docker-compose -f docker-peer1.yaml kill && docker-compose -f docker-peer1.yaml down
docker-compose -f docker-peer.yaml kill && docker-compose -f docker-peer.yaml down
docker-compose -f docker-peer3.yaml kill && docker-compose -f docker-peer3.yaml down
docker-compose -f docker-peer4.yaml kill && docker-compose -f docker-peer4.yaml down

# Your system is now clean
