CREATE TABLE `Sales` (`Number` VARCHAR(10), `CustNumber` VARCHAR(10), `Date` DATE)
CREATE TABLE `Lines` (`SONumber` VARCHAR(10), `Number` INTEGER, `Part` VARCHAR(10), `Quantity` INTEGER)
CREATE TABLE `Customers` (`Number` VARCHAR(10), `Name` VARCHAR(40), `Stree` VARCHAR(50), `City` VARCHAR(50), `State` VARCHAR(2), `PostalCode` VARCHAR(10)
CREATE TABLE `Parts` (`Number` VARCHAR(10), `Description` VARCHAR(255), `Price` DOUBLE)