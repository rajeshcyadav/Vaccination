# TODO

-[X] redirect to co win
-[X] Start from validation -> validate and add data to object -> send data to db
-[X] update counter of hospital from admin when new request is given to hospital
-[X] update counter of global variable  "requestCounter" from user when user makes new vaccine
 request.
-[X] update counter of global variable  "userCounter" when new user is created (SignUp).
-[X] Remove getContext() before createView method. i.e on instance variable;
-[ ] lock orientation
 ~~-[ ] admin add Vaccine to Vaccine Node~
-[X] Create User Request
-[X] Create User Record
-[X] check vaccine decrement logic in hospital 
-[X] sign out button in profile page.
~~-[] show cancel dialog box for reason of cancellation of vac request~~
-[X] change contact to number only in vaccination registration. 

## Rewrite

-[X] change request and record node in user,admin and hospital
-[X] change from request to record when anyone cancels or denies the request.

- ### Requests
    -[X] Admin get all data from request where status is waiting.
    -[X] Hospital get all data where status is "Approved" and hospital uid matches.
    -[X] User will get all data

## Suggestions

~~- bottom navigation in Admin main and Hospital Main~~
~~- Requests, Stock , Profile~~
~~- get all hospital -> sort ->check if vaccine uid exist in list -> if true show hospital else move
to next~~
~~- remove Hospital from sign up fragment~~
~~- long press for vaccine deletion -for deletion of vaccine show text which notifies user about
long press~~
