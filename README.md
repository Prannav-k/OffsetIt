# Carbon offset - Prannav K

## Introduction
A distributed ledger application where carbon offsets can be issued , transferred and redeemed between organizations .

## Background

Carbon offset is a way of removing same or even more emissions than an organization can emit. This procedure would be very helpful especially in industries like Airline where no better alternatives can be provided at this point.

But how do multiple organisations, regulating bodies and governments of different countries come together and trust a single platform? There comes this solution where multiple parties can come together and transact their offsets in a trustful way.

Note: This app's primary aim is show how multiple parties can come up and transact credits/offsets in a trustful , tamperproof way. App assumes the source of offsets and governance rules are reliable. This is initial version of the app developed as part of corda dev week apac 2021 thus leaving a lot of scope for developing a complete solution.

### Participants

1. Verifier (Individual authorised party to verify the offset and issue them with offset token)
2. GreenCo (An organization that does the carbon offset like rain forest preservation, wetland restoration or works on environmental tech)
3. Org1 (An airline organization that is committed to reduce its carbon footprint)

### Sequence and Flows

Below is the sequence that will be demonstrated

1. Initially we will issue some USD using `IssueFiatFlow`.
2. Then verifier once verified will issue an offset token using `CreateAndIssueOffsetToken`.
3. An organisation can request for a transfer / buy offset token using  `CreateTransferRequest`. (Note : For buying/transfer a request and accept mechanism is developed to avoid misuse of direct transfer)
4. The green co then can accept the request using `AcceptTransferRequest`



## Pre-Requisites
For development environment setup, please refer to: [Setup Guide](https://docs.corda.net/getting-set-up.html).

# Usage

## Bootstraping the nodes
We have predefined nodes mentioned in build.gradle. Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```
## Invoking the flows

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue July 09 11:58:13 GMT 2019>>>

You can use this shell to interact with your node.

Let's start by going to the shell of Verifier and issue some USD to Org1 (Issuing should in real be done by federal/reserve bank). Org1 will need the fiat currency to exchange it for the Offset Token. 

    start IssueFiatFlow currency: USD, amount: 2000000, recipient: Org1

In the same shell, lets issue an offset token to green co for their good work. The output nft id will be used for further transfer

    start CreateAndIssueOffsetToken owner: GreenCo, offsetPrice: 10000 USD, offsetType: carbon(co2), offsetUnit: ton, source: 122322, otherInfo: RainforestPreservation, expiryDays: 365

After offset token getting issues, we . Goto Org1's shell to create a transfer req id will be used further for transfer.

    start CreateTransferRequestInitiator requestTo: GreenCo

From Green co's shell we can now check the transfer requests received. The output transfer
    
    run vaultQuery contractStateType: com.prannav.carbonOffset.states.TransferRequestState

From the same shell , lets accept the request. Note : Transfer req id and offset Id are available as responses from above steps or can be fetched from below mentioned vault query commands. 

    start AcceptTransferRequest transferReqId: <transferReqId>, offsetId: <offsetId>

Below commands can be used to verify the transfers in each node

Command to fetch the fungible tokens available in the vault (fiat USD in this case)

    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken

Command to fetch non-fungible tokens available in the vault (offset token in this case)

    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
