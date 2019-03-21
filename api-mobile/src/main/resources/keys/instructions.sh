#!/bin/bash

# create PK
openssl ecparam -name secp256r1 -genkey -out private.pem

# convert to pkcs8
openssl pkcs8 -topk8 -nocrypt -in private.pem -out private_key.pem

# create PubK
openssl ec -in private.pem -pubout -out public.pem