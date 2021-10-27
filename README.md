# Carbon offset - Prannav K

## Introduction
A distributed ledger application where carbon offsets can be issued , transferred and redeemed between organizations .

## Background

Carbon offset is a way of removing same or even more emissions than an organization can emit. This procedure would be very helpful especially in industries like Airline where no better alternatives can be provided at this point.

But how do multiple organisations, regulating bodies and governments of different countries come together and trust a single platform? There comes this solution where multiple parties can come together and transact their offsets in a trustful way.

Note: This app's primary aim is show how multiple parties can come up and transact credits/offsets in a trustful , tamperproof way. App assumes the source of offsets and governance rules are reliable. This is initial version of the app developed as part of corda dev week apac 2021 thus leaving a lot of scope for developing a complete solution.

### Flows

There are three flows that we'll primarily use in this example that you'll be building off of.

1. We'll start with running `FiatCurrencyIssueFlow`.
2. We'll then create and issue a house token using `HouseTokenCreateAndIssueFlow`.
3. We'll then initiate the sale of the house through `HouseSaleInitiatorFlow`.



## Pre-Requisites
For development environment setup, please refer to: [Setup Guide](https://docs.corda.net/getting-set-up.html).

# Usage

## Running the nodes
Open a terminal and go to the project root directory and type: (to deploy the nodes using bootstrapper)
```
./gradlew clean deployNodes
```
Then type: (to run the nodes)
```
./build/nodes/runnodes
```
## Interacting with the nodes

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue July 09 11:58:13 GMT 2019>>>

You can use this shell to interact with your node.

First go to the shell of PartyA and issue some USD to Party C. We will need the fiat currency to exchange it for the house token. 

    start FiatCurrencyIssueFlow currency: USD, amount: 100000000, recipient: PartyC

    start FiatCurrencyIssueFlow currency: USD, amount: 2000000, recipient: Org1

We can now go to the shell of PartyC and check the amount of USD issued. Since fiat currency is a fungible token we can query the vault for [FungibleToken](https://training.corda.net/libraries/tokens-sdk/#fungibletoken) states.

    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken
    
Once we have the USD issued to PartyC, we can Create and Issue the HouseToken to PartyB. Goto PartyA's shell to create and issue the house token.

    start FiatCurrencyIssueFlow currency: USD, amount: 2000000, recipient: Org1
    start CreateAndIssueOffsetToken owner: GreenCo, offsetPrice: 10000 USD, offsetType: carbon(co2), offsetUnit: ton, source: 122322, otherInfo: RainforestPreservation, expiryDays: 365
    start CreateTransferRequestInitiator requestTo: GreenCo
    run vaultQuery contractStateType: com.prannav.carbonOffset.states.TransferRequestState
    start AcceptTransferRequest transferReqId: 2d7eec9f-475d-4438-995b-9ced1e7ffb3e, offsetId: fb228141-b284-4cd7-ba77-1d5826a22171

We can now check the issued house token in PartyB's vault. Since we issued it as a [NonFungible](https://training.corda.net/libraries/tokens-sdk/#nonfungibletoken) token we can query the vault for non-fungible tokens.
    
    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
    
Note that HouseState token is an evolvable token which is a [LinearState](https://docs.corda.net/docs/corda-os/api-states.html#linearstate), thus we can check PartyB's vault to view the [EvolvableToken](https://training.corda.net/libraries/tokens-sdk/#evolvabletokentype)

    run vaultQuery contractStateType: com.prannav.carbonOffset.states.OffsetTokenState
    
Note the linearId of the HouseState token from the previous step, we will need it to perform our DvP opearation. Goto PartyB's shell to initiate the token sale.
    
    start HouseSale houseId: <XXXX-XXXX-XXXX-XXXXX>, buyer: PartyC
    454DFDEDA82FAF097F550760C502A21CD08BADB9878E8632BFD3C1461B21EB6A
    start SellOffset transferReqId: 06f45506-09bd-4933-a816-eaacb071f158, offsetId: af293754-1e14-4f2c-9349-10daf87c395a
    af293754-1e14-4f2c-9349-10daf87c395a
    start HouseSale houseId: 9de61875-2752-4b9e-9d0c-0beadcbdc2ae, buyer: PartyC

We could now verify that the non-fungible token has been transferred to PartyC and some 100,000 USD from PartyC's vault has been transferred to PartyB. Run the below commands in PartyB and PartyC's shell to verify the same
    
    // Run on PartyB's shell
    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.FungibleToken
    // Run on PartyC's shell
    run vaultQuery contractStateType: com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
