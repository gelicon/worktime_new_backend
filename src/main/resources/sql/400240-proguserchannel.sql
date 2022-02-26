CREATE TABLE proguserchannel(
	proguserchannel_id INTEGER NOT NULL,
	proguser_id INTEGER NOT NULL,
	channelnotification_id INTEGER NOT NULL,
	proguserchannel_address VARCHAR(125),
    PRIMARY KEY (proguserchannel_id),
    CONSTRAINT proguserchannel_ak1 UNIQUE (proguser_id, channelnotification_id),
    CONSTRAINT proguserchannel_fk1 FOREIGN KEY (proguser_id) REFERENCES proguser(proguser_id)
      ON UPDATE CASCADE  ON DELETE CASCADE,
    CONSTRAINT proguserchannel_fk2 FOREIGN KEY (channelnotification_id) REFERENCES capcode(capcode_id)
);
CREATE SEQUENCE proguserchannel_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE proguserchannel_id_gen OWNED BY proguserchannel.proguserchannel_id;
