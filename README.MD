
run from root (it needs jdk 1.8 and docker):
./gradlew build -x test && docker-compose up

Requests:
1. Create account: curl -X POST http://localhost:9090/accounts
    response example:
     body: {"id":1,"balance":0}, response status: 201
    
2. Get account info: curl http://localhost:9090/accounts/1
    response example:
     body: {"id":1,"balance":0}, response status: 200
     
3. Deposit: curl -X POST http://localhost:9090/accounts/1/deposit
    response example:
     body: , response status: 202
          
4. Withdrawal: curl -X POST http://localhost:9090/accounts/1/withdrawal
    response example:
     body: , response status: 202
     
ERRORS:
1. Id not found (get, deposit or withdrawal operations)
    response example:
     body: Account with id='2' not found,  response status: 404
     
2. Not enough money (withdrawal operation)
    response example:
     body: Not enough money,  response status: 403
          
3. Sum isn't positive (deposit or withdrawal operations)
    response example:
     body: Sum must be positive,  response status: 400
