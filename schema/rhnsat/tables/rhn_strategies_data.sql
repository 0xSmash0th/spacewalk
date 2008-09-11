--
--$Id$
--
-- 
--

--data for rhn_strategies (has a sequence!!!)

insert into rhn_strategies(recid,name,comp_crit,esc_crit,contact_strategy,
ack_completed) 
    values (rhn_strategies_recid_seq.nextval,'Broadcast-NoAck',
    'Sent=100',NULL,'Broadcast','No');
insert into rhn_strategies(recid,name,comp_crit,esc_crit,contact_strategy,
ack_completed) 
    values (rhn_strategies_recid_seq.nextval,'Escalate-OneAck',
    'Acked>0','Incomplete=0','Escalate','One');
insert into rhn_strategies(recid,name,comp_crit,esc_crit,contact_strategy,
ack_completed) 
    values (rhn_strategies_recid_seq.nextval,'Escalate-AllAck',
    'Acked=100','(Failed>50)|(Incomplete=0)','Escalate','All');
insert into rhn_strategies(recid,name,comp_crit,esc_crit,contact_strategy,
ack_completed) 
    values (rhn_strategies_recid_seq.nextval,'Broadcast-OneAck',
    'Acked>0',NULL,'Broadcast','One');
insert into rhn_strategies(recid,name,comp_crit,esc_crit,contact_strategy,
ack_completed) 
    values (rhn_strategies_recid_seq.nextval,'Broadcast-AllAck',
    'Acked=100',NULL,'Broadcast','All');

--Increment the sequence so the next value is 12
-- These select 6 through 11.
select rhn_strategies_recid_seq.nextval from dual; 
select rhn_strategies_recid_seq.nextval from dual;
select rhn_strategies_recid_seq.nextval from dual;
select rhn_strategies_recid_seq.nextval from dual;
select rhn_strategies_recid_seq.nextval from dual;
select rhn_strategies_recid_seq.nextval from dual;

insert into rhn_strategies(recid,name,comp_crit,esc_crit,contact_strategy,
ack_completed) 
    values (rhn_strategies_recid_seq.nextval,'Escalate-NoAck',
    'Sent>0','Failed>0|Incomplete>0','Escalate','No');
commit;

--$Log$
--Revision 1.6  2004/06/17 20:48:59  kja
--bugzilla 124970 -- _data is in for 350.
--
--Revision 1.5  2004/05/29 21:51:49  pjones
--bugzilla: none -- _data is not for 340, so says kja.
--
--Revision 1.4  2004/05/28 22:25:50  pjones
--bugzilla: none -- no comments after ;, they don't work.
--
--Revision 1.3  2004/05/04 20:03:38  kja
--Added commits.
--
--Revision 1.2  2004/04/22 19:05:45  kja
--Added the 24 x 7 schedule data.  Corrected logic for skipping sequence numbers
--in rhn_notification_formats_data.sql and rhn_strategies_data.sql.
--
--Revision 1.1  2004/04/22 17:49:49  kja
--Added data for the reference tables.
--
