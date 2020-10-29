-- Drop foreign keys from ART database tables

-- NOTES:
-- for mysql, replace DROP CONSTRAINT with DROP FOREIGN KEY

-- ------------------------

ALTER TABLE ART_USERS DROP CONSTRAINT art_u_fk_acc_lvl;
ALTER TABLE ART_USERS DROP CONSTRAINT art_u_fk_def_qr_grp;
ALTER TABLE ART_USER_GROUPS DROP CONSTRAINT art_ug_fk_dqg;
ALTER TABLE ART_USER_USERGROUP_MAP DROP CONSTRAINT art_uugm_fk_ug_id;
ALTER TABLE ART_USER_USERGROUP_MAP DROP CONSTRAINT art_uugm_fk_u_id;
ALTER TABLE ART_QUERIES DROP CONSTRAINT art_q_fk_qt;
ALTER TABLE ART_QUERIES DROP CONSTRAINT art_q_fk_ds_id;
ALTER TABLE ART_QUERIES DROP CONSTRAINT art_q_fk_enc_id;
ALTER TABLE ART_REPORT_REPORT_GROUPS DROP CONSTRAINT art_rrg_fk_r_id;
ALTER TABLE ART_REPORT_REPORT_GROUPS DROP CONSTRAINT art_rrg_fk_rg_id;
ALTER TABLE ART_ADMIN_PRIVILEGES DROP CONSTRAINT art_admpv_fk_u_id;
ALTER TABLE ART_USER_REPORT_MAP DROP CONSTRAINT art_urm_fk_u_id;
ALTER TABLE ART_USER_REPORT_MAP DROP CONSTRAINT art_urm_fk_r_id;
ALTER TABLE ART_USER_REPORTGROUP_MAP DROP CONSTRAINT art_urgm_fk_u_id;
ALTER TABLE ART_USER_REPORTGROUP_MAP DROP CONSTRAINT art_urgm_fk_rg_id;
ALTER TABLE ART_PARAMETERS DROP CONSTRAINT art_pmt_fk_dv_r_id;
ALTER TABLE ART_PARAMETERS DROP CONSTRAINT art_pmt_fk_lov_r_id;
ALTER TABLE ART_REPORT_PARAMETERS DROP CONSTRAINT art_rpmt_fk_r_id;
ALTER TABLE ART_REPORT_PARAMETERS DROP CONSTRAINT art_rpmt_fk_p_id;
ALTER TABLE ART_QUERY_RULES DROP CONSTRAINT art_qrul_fk_q_id;
ALTER TABLE ART_QUERY_RULES DROP CONSTRAINT art_qrul_fk_r_id;
ALTER TABLE ART_USER_RULES DROP CONSTRAINT art_urul_fk_u_id;
ALTER TABLE ART_USER_RULES DROP CONSTRAINT art_urul_fk_r_id;
ALTER TABLE ART_USER_GROUP_RULES DROP CONSTRAINT art_ugrul_fk_ug_id;
ALTER TABLE ART_USER_GROUP_RULES DROP CONSTRAINT art_ugrul_fk_r_id;
ALTER TABLE ART_JOBS DROP CONSTRAINT art_jb_fk_q_id;
ALTER TABLE ART_JOBS DROP CONSTRAINT art_jb_fk_u_id;
ALTER TABLE ART_JOBS DROP CONSTRAINT art_jb_fk_r_q_id;
ALTER TABLE ART_JOBS DROP CONSTRAINT art_jb_fk_s_id;
ALTER TABLE ART_JOBS DROP CONSTRAINT art_jb_fk_ss_id;
ALTER TABLE ART_JOBS DROP CONSTRAINT art_jb_fk_cd_id;
ALTER TABLE ART_JOBS_PARAMETERS DROP CONSTRAINT art_jbpmt_fk_j_id;
ALTER TABLE ART_USER_JOB_MAP DROP CONSTRAINT art_ujbm_fk_j_id;
ALTER TABLE ART_USER_JOB_MAP DROP CONSTRAINT art_ujbm_fk_u_id;
ALTER TABLE ART_USER_JOB_MAP DROP CONSTRAINT art_ujbm_fk_ug_id;
ALTER TABLE ART_USER_GROUP_QUERIES DROP CONSTRAINT art_ugq_fk_ug_id;
ALTER TABLE ART_USER_GROUP_QUERIES DROP CONSTRAINT art_ugq_fk_q_id;
ALTER TABLE ART_USER_GROUP_GROUPS DROP CONSTRAINT art_uggr_fk_ug_id;
ALTER TABLE ART_USER_GROUP_GROUPS DROP CONSTRAINT art_uggr_fk_qg_id;
ALTER TABLE ART_USER_GROUP_JOBS DROP CONSTRAINT art_ugjb_fk_ug_id;
ALTER TABLE ART_USER_GROUP_JOBS DROP CONSTRAINT art_ugjb_fk_j_id;
ALTER TABLE ART_DRILLDOWN_QUERIES DROP CONSTRAINT art_drq_fk_q_id;
ALTER TABLE ART_DRILLDOWN_QUERIES DROP CONSTRAINT art_drq_fk_dq_id;
ALTER TABLE ART_JOB_ARCHIVES DROP CONSTRAINT art_jbar_fk_j_id;
ALTER TABLE ART_JOB_ARCHIVES DROP CONSTRAINT art_jbar_fk_u_id;
ALTER TABLE ART_LOGGED_IN_USERS DROP CONSTRAINT art_lgu_fk_u_id;
ALTER TABLE ART_SCHEDULE_HOLIDAY_MAP DROP CONSTRAINT art_schhldm_fk_s_id;
ALTER TABLE ART_SCHEDULE_HOLIDAY_MAP DROP CONSTRAINT art_schhldm_fk_h_id;
ALTER TABLE ART_JOB_HOLIDAY_MAP DROP CONSTRAINT art_jbhldm_fk_j_id;
ALTER TABLE ART_JOB_HOLIDAY_MAP DROP CONSTRAINT art_jbhldm_fk_h_id;
ALTER TABLE ART_JOB_DESTINATION_MAP DROP CONSTRAINT art_jbdstm_fk_j_id;
ALTER TABLE ART_JOB_DESTINATION_MAP DROP CONSTRAINT art_jbdstm_fk_d_id;
ALTER TABLE ART_SAVED_PARAMETERS DROP CONSTRAINT art_svpmt_fk_u_id;
ALTER TABLE ART_SAVED_PARAMETERS DROP CONSTRAINT art_svpmt_fk_r_id;
ALTER TABLE ART_USER_PARAM_DEFAULTS DROP CONSTRAINT art_upardf_fk_u_id;
ALTER TABLE ART_USER_PARAM_DEFAULTS DROP CONSTRAINT art_upardf_fk_p_id;
ALTER TABLE ART_USER_GROUP_PARAM_DEFAULTS DROP CONSTRAINT art_ugpardf_fk_ug_id;
ALTER TABLE ART_USER_GROUP_PARAM_DEFAULTS DROP CONSTRAINT art_ugpardf_fk_p_id;
ALTER TABLE ART_USER_FIXED_PARAM_VAL DROP CONSTRAINT art_ufxparvl_fk_u_id;
ALTER TABLE ART_USER_FIXED_PARAM_VAL DROP CONSTRAINT art_ufxparvl_fk_p_id;
ALTER TABLE ART_USER_GROUP_FIXED_PARAM_VAL DROP CONSTRAINT art_ugfxparvl_fk_ug_id;
ALTER TABLE ART_USER_GROUP_FIXED_PARAM_VAL DROP CONSTRAINT art_ugfxparvl_fk_p_id;
ALTER TABLE ART_ROLE_PERMISSION_MAP DROP CONSTRAINT art_rolpermm_fk_r_id;
ALTER TABLE ART_ROLE_PERMISSION_MAP DROP CONSTRAINT art_rolpermm_fk_p_id;
ALTER TABLE ART_USER_ROLE_MAP DROP CONSTRAINT art_urolm_fk_u_id;
ALTER TABLE ART_USER_ROLE_MAP DROP CONSTRAINT art_urolm_fk_r_id;
ALTER TABLE ART_USER_GROUP_ROLE_MAP DROP CONSTRAINT art_ugrolm_fk_ug_id;
ALTER TABLE ART_USER_GROUP_ROLE_MAP DROP CONSTRAINT art_ugrolm_fk_r_id;
ALTER TABLE ART_USER_PERMISSION_MAP DROP CONSTRAINT art_upermm_fk_u_id;
ALTER TABLE ART_USER_PERMISSION_MAP DROP CONSTRAINT art_upermm_fk_p_id;
ALTER TABLE ART_USER_GROUP_PERM_MAP DROP CONSTRAINT art_ugpermm_fk_ug_id;
ALTER TABLE ART_USER_GROUP_PERM_MAP DROP CONSTRAINT art_ugpermm_fk_p_id;
ALTER TABLE ART_PIPELINES DROP CONSTRAINT art_ppln_fk_s_id;
