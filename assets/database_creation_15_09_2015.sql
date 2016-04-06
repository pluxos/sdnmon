CREATE SCHEMA outro
  AUTHORIZATION postgres;

CREATE TABLE outro.action
(
  flow_id integer NOT NULL,
  switch_dpid character varying(30) NOT NULL,
  type character varying(30),
  id integer,
  CONSTRAINT "actionPK" PRIMARY KEY (flow_id, switch_dpid),
  CONSTRAINT "action_matchFK" FOREIGN KEY (flow_id, switch_dpid)
      REFERENCES outro.match (flow_id, switch_dpid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE outro.action
  OWNER TO postgres;


CREATE TABLE outro.flowstats
(
  flow_id integer NOT NULL,
  switch_dpid character varying(30) NOT NULL,
  "table" integer,
  packet_count bigint,
  byte_count bigint,
  transmission_rate double precision,
  unit character varying(30),
  "time" bigint,
  CONSTRAINT "flowstatsPK" PRIMARY KEY (flow_id, switch_dpid),
  CONSTRAINT "flow_matchFK" FOREIGN KEY (flow_id, switch_dpid)
      REFERENCES outro.match (flow_id, switch_dpid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE outro.flowstats
  OWNER TO postgres;


CREATE TABLE outro.match
(
  source_ip character varying(20),
  destionation_ip character varying(20),
  transport_protocol character varying(20),
  source_port integer,
  destination_port integer,
  flow_id integer NOT NULL,
  switch_dpid character varying(30) NOT NULL,
  CONSTRAINT "matchPK" PRIMARY KEY (flow_id, switch_dpid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE outro.match
  OWNER TO postgres;

CREATE TABLE outro.threads
(
  name character varying(50) NOT NULL,
  switch_id character varying(30),
  outport integer,
  source_ip character varying(30),
  destination_ip character varying(30),
  status character varying(20),
  CONSTRAINT "threadsPK" PRIMARY KEY (name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE outro.threads
  OWNER TO postgres;


