--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

ALTER TABLE `mpesa_request_response`
	ADD COLUMN `amount` DECIMAL(10,2) NULL AFTER `transaction_id`,
	ADD COLUMN `providerID` INT NULL AFTER `amount`,
	ADD COLUMN `narration` VARCHAR(100) NULL AFTER `providerID`,
	ADD COLUMN `beneficiaryAccount` VARCHAR(50) NULL AFTER `narration`,
	ADD COLUMN `senderName` VARCHAR(50) NULL AFTER `beneficiaryAccount`;