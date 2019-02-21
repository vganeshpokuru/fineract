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

INSERT INTO `c_configuration` (`name`, `enabled`) VALUES ('allow-bank-transactions', 1);

CREATE TABLE `f_bank_name_mpesa` (
	`bank_id` VARCHAR(50) NULL,
	`bank_name` VARCHAR(50) NULL
)
ENGINE=InnoDB
COLLATE='utf8_general_ci';

CREATE TABLE `f_bank_mpesa_details` (
	`property_name` VARCHAR(50) NULL,
	`property_description` VARCHAR(50) NULL
)
ENGINE=InnoDB
COLLATE='utf8_general_ci';

CREATE TABLE `mpesa_request_response` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`request_date_time` DATETIME NULL DEFAULT NULL,
	`request_for` VARCHAR(50) NULL DEFAULT NULL,
	`request_done_by` BIGINT(20) NULL DEFAULT NULL,
	`request_body` TEXT NULL,
	`response_date_time` DATETIME NULL DEFAULT NULL,
	`response_body` TEXT NULL,
	`transaction_id` TEXT NULL,
	PRIMARY KEY (`id`),
	INDEX `FK_mpesa_request_response_m_appuser` (`request_done_by`),
	CONSTRAINT `FK_mpesa_request_response_m_appuser` FOREIGN KEY (`request_done_by`) REFERENCES `m_appuser` (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('wallet_number', '8880170000000017');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('server_ip', 'testids.interswitch.co.ke');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('server_port', '19081');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('mini_statement_URL', '/api/v1/wallet/wallets/transactions/ministatement/');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('server_client_id', 'IKIAA9E210AA86A425B00F931B6F25D21441F7874B67');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('server_client_secret_key', 'zcMFMaVwzu9FQYT0NlzU1vZNOTmt3al8UyNhqFPiWDI=');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('time_zone', 'Africa/Lagos');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('signature_method', 'SHA1');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('currency_code', '404');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('transaction_type', '9002');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('transaction_starting', 'IMARA');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('processing_code', '502020');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('balance_URL', '/api/v1/wallet/wallets/balance/');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('transaction_URL', '/api/v1/wallet/wallets/transactions');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('mobile_no', '735402848');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('mobile_no', 'FxgWlbnnmaOm3dyCWkNikWtB9rtH7su8dJPttnRTjIO7tciBW37dR3U8qqa3');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('contry_code', '+254');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('account_id', '222200');

INSERT INTO `f_bank_mpesa_details` (`property_name`, `property_description`) VALUES ('account_name', 'TIMIZACPTAL');

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'READ_MPESA', 'MPESA', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'VALIDATE_MPESA', 'MPESA', 'VALIDATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'READ_MPESAMINISTATEMENT', 'MPESAMINISTATEMENT', 'READ', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'PROCEED_BANKTRANSACTION', 'BANKTRANSACTION', 'PROCEED', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'CHECK_MPESABALANCE', 'MPESABALANCE', 'CHECK', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'DO_MPESABANKTRANSACTION', 'MPESABANKTRANSACTION', 'DO', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'VALIDATE_MPESA_OTP', 'MPESA_OTP', 'VALIDATE', 0);

INSERT INTO `m_permission` (`grouping`, `code`, `entity_name`, `action_name`, `can_maker_checker`) VALUES ('mpesa', 'RESEND_MPESA_OTP', 'RESEND_MPESA_OTP', 'RESEND', 0);