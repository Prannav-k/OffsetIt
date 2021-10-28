# Carbon offset - Prannav K

## Introduction
A distributed ledger application where carbon offsets can be issued , transferred and redeemed between organizations .

## Background

Carbon offset is a way of removing equal or even more emissions than an organization can emit. This procedure would be very helpful especially in industries like Airline where no better alternatives can be provided at this point.

But how do multiple organisations, regulating bodies and governments of different countries come together and trust a single platform? There comes this solution where multiple parties can come together and issue and transact their carbon/other offsets as NFT (non-fungible token) in a trustful way.

Note: This app's primary aim is show how multiple parties can come up and transact credits/offsets in a trustful , tamper-proof way. App assumes the source of offsets and governance rules are reliable. This is initial version of the app developed rapidly as part of submission for corda dev week apac 2021 thus leaving a lot of scope for developing a complete solution.

### Participants

1. MinistryOfEnvironment (Govt authorised party to verify the offset and issue the offset source with offset token)
2. GreenCo (An organization that does the carbon offset like rain forest preservation, wetland restoration or works on environmental tech)
3. Org1 (An airline organization that is committed to reduce its carbon footprint)

### Sequence and Flows

Below is the sequence that will be demonstrated

1. Initially we will issue some USD using `IssueFiatFlow`.
2. Then MinistryOfEnvironment once verifying the offset authenticity will issue an offset token using `CreateAndIssueOffsetToken`.
3. Org1 can request for a transfer / buy offset token from Green Co using  `CreateTransferRequest`. (Note : For buying/transfer a request and accept mechanism is developed to avoid misuse of direct transfer)
4. The Green Co then can accept the request using `AcceptTransferRequest` which will transfer the offset token in exchange of fiat tokens.
5. Finally, the org1 can redeem the offset token using `RedeemOffsetFlow` with govt/Ministry of Environment. This can be to claim incentives or write off pollution tax.


## Pre-Requisites
1. Java 8
2. Minimum of 8gb memory
3. xterm for runNodes

Refer https://docs.r3.com/en/platform/corda/4.8/open-source/getting-set-up.html for  more.
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

Above command will start all the nodes. Alternatively we can navigate to individual nodes and perform java -jar corda.jar

Use each node's shell to perform below commands.

Let's start by going to the shell of MinistryOfEnvironment and issue some USD to Org1 (Issuing should in real be done by federal/reserve bank). Org1 will need the fiat currency to buy Offset Token. 

    start IssueFiatFlow currency: USD, amount: 2000000, recipient: Org1

In the same shell, lets issue an offset token to green co for their good work. The output nft id will be used for further transfer

    start CreateAndIssueOffsetToken owner: GreenCo, offsetPrice: 10000 USD, offsetType: carbon(co2), offsetUnit: ton, source: 122322, otherInfo: RainforestPreservation, expiryDays: 365

Note : Use the commands mentioned in the bottom to query the ledger and confirm the transfers and updates.

After offset token getting issues, we . Goto Org1's shell to create a transfer req id will be used further for transfer.

    start CreateTransferRequestInitiator requestTo: GreenCo

From Green co's shell we can now check the transfer requests received. The output transfer
    
    run vaultQuery contractStateType: com.prannav.carbonOffset.states.TransferRequestState

From the same shell , lets accept the request. Note : Transfer req id and offset Id are available as responses from above steps or can be fetched from below mentioned vault query commands. 

    start AcceptTransferRequest transferReqId: <transferReqId>, offsetId: <offsetId>
    sample : start AcceptTransferRequest transferReqId: b486f83f-0421-4cf0-9b24-d35c2ee63dc7, offsetId: 053a2324-5f29-4d55-890e-bf89efd6198a

Now from org1 shell, lets redeem the offset token.

    start RedeemOffsetFlow offsetId: <offsetId>
    sample : start RedeemOffsetFlow offsetId: 053a2324-5f29-4d55-890e-bf89efd6198a


Below commands can be used to verify the transfers in each node

Command to fetch the fungible tokens available in the vault (fiat USD in this case)

    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken

Command to fetch non-fungible tokens available in the vault (offset token in this case)

    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken

## Going further

1. Network can be organised with roles using business network/membership utils thus having role based check on who can issue tokens.
2. Have the environmental tax / rebate in exchange for redeeming offset nft with govt with another ledger native asset.
3. More fine defined and bulk transfer requests. 
