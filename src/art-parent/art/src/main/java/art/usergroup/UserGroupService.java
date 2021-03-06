/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.usergroup;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.user.User;
import art.general.ActionResult;
import art.permission.Permission;
import art.permission.PermissionService;
import art.role.Role;
import art.role.RoleService;
import art.usergrouppermission.UserGroupPermissionService;
import art.usergrouprole.UserGroupRoleService;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, deleting and updating user groups
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

	private final DbService dbService;
	private final ReportGroupService reportGroupService;
	private final RoleService roleService;
	private final PermissionService permissionService;
	private final UserGroupPermissionService userGroupPermissionService;
	private final UserGroupRoleService userGroupRoleService;

	@Autowired
	public UserGroupService(DbService dbService, ReportGroupService reportGroupService,
			RoleService roleService, PermissionService permissionService,
			UserGroupPermissionService userGroupPermissionService,
			UserGroupRoleService userGroupRoleService) {

		this.dbService = dbService;
		this.reportGroupService = reportGroupService;
		this.roleService = roleService;
		this.permissionService = permissionService;
		this.userGroupPermissionService = userGroupPermissionService;
		this.userGroupRoleService = userGroupRoleService;
	}

	public UserGroupService() {
		dbService = new DbService();
		reportGroupService = new ReportGroupService();
		roleService = new RoleService();
		permissionService = new PermissionService();
		userGroupPermissionService = new UserGroupPermissionService();
		userGroupRoleService = new UserGroupRoleService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USER_GROUPS AUG";

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			UserGroup group = new UserGroup();

			group.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			group.setName(rs.getString("NAME"));
			group.setDescription(rs.getString("DESCRIPTION"));
			group.setStartReport(rs.getString("START_QUERY"));
			group.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			group.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			group.setCreatedBy(rs.getString("CREATED_BY"));
			group.setUpdatedBy(rs.getString("UPDATED_BY"));

			ReportGroup defaultReportGroup = reportGroupService.getReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
			group.setDefaultReportGroup(defaultReportGroup);

			List<Role> roles = roleService.getRolesForUserGroup(group.getUserGroupId());
			group.setRoles(roles);

			List<Permission> permissions = permissionService.getPermissionsForUserGroup(group.getUserGroupId());
			group.setPermissions(permissions);

			return type.cast(group);
		}
	}

	/**
	 * Returns all user groups
	 *
	 * @return all user groups
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public List<UserGroup> getAllUserGroups() throws SQLException {
		logger.debug("Entering getAllUserGroups");

		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns user groups with given ids
	 *
	 * @param ids comma separated string of the user group ids to retrieve
	 * @return user groups with given ids
	 * @throws SQLException
	 */
	public List<UserGroup> getUserGroups(String ids) throws SQLException {
		logger.debug("Entering getUserGroups: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		if (idsArray.length == 0) {
			return new ArrayList<>();
		}

		String sql = SQL_SELECT_ALL
				+ " WHERE USER_GROUP_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupService.UserGroupMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns the user group with the given id
	 *
	 * @param id the user group id
	 * @return the user group if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public UserGroup getUserGroup(int id) throws SQLException {
		logger.debug("Entering getUserGroup: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE USER_GROUP_ID=?";
		ResultSetHandler<UserGroup> h = new BeanHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns the user group with the given name
	 *
	 * @param name the user group name
	 * @return the user group if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public UserGroup getUserGroup(String name) throws SQLException {
		logger.debug("Entering getUserGroup: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<UserGroup> h = new BeanHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Returns the user groups that the given user belongs to
	 *
	 * @param userId the user id
	 * @return the user's user groups
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public List<UserGroup> getUserGroupsForUser(int userId) throws SQLException {
		logger.debug("Entering getUserGroupsForUser: userId={}", userId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_USER_USERGROUP_MAP AUUGM"
				+ " ON AUUGM.USER_GROUP_ID=AUG.USER_GROUP_ID"
				+ " WHERE AUUGM.USER_ID=?"
				+ " ORDER BY AUG.USER_GROUP_ID"; //have order by so that effective values are deterministic
		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Deletes the user group with the given id
	 *
	 * @param id the user group id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * users which prevented the user group from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public ActionResult deleteUserGroup(int id) throws SQLException {
		logger.debug("Entering deleteUserGroup: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedUsers = getLinkedUsers(id);
		if (!linkedUsers.isEmpty()) {
			result.setData(linkedUsers);
			return result;
		}

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_USER_GROUP_RULES WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_JOB_MAP WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_USERGROUP_MAP WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_QUERIES WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_GROUPS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_JOBS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_PARAM_DEFAULTS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_FIXED_PARAM_VAL WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_PERM_MAP WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		//finally delete user group
		sql = "DELETE FROM ART_USER_GROUPS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes the user groups with the given ids
	 *
	 * @param ids the user group ids
	 * @return ActionResult. if not successful, data contains details of the
	 * user groups that were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public ActionResult deleteUserGroups(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteUserGroups: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteUserGroup(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedUsers = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedUsers, ", ");
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}

		return result;
	}

	/**
	 * Adds a new user group
	 *
	 * @param group the user group
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public synchronized int addUserGroup(UserGroup group, User actionUser) throws SQLException {
		logger.debug("Entering addUserGroup: group={}, actionUser={}", group, actionUser);

		//generate new id
		String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
		int newId = dbService.getNewRecordId(sql);

		saveUserGroup(group, newId, actionUser);

		return newId;
	}

	/**
	 * Updates a user group
	 *
	 * @param group the updated user group
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public void updateUserGroup(UserGroup group, User actionUser) throws SQLException {
		Connection conn = null;
		updateUserGroup(group, actionUser, conn);
	}

	/**
	 * Updates a user group
	 *
	 * @param group the updated user group
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public void updateUserGroup(UserGroup group, User actionUser, Connection conn)
			throws SQLException {

		logger.debug("Entering updateUserGroup: group={}, actionUser={}", group, actionUser);

		Integer newRecordId = null;
		saveUserGroup(group, newRecordId, actionUser, conn);
	}

	/**
	 * Imports user group records
	 *
	 * @param userGroups the list of user groups to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void importUserGroups(List<UserGroup> userGroups, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importUserGroups: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
			int userGroupId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS";
			int reportGroupId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(ROLE_ID) FROM ART_ROLES";
			int roleId = dbService.getMaxRecordId(conn, sql);

			List<UserGroup> currentUserGroups = new ArrayList<>();
			if (overwrite) {
				currentUserGroups = getAllUserGroups();
			}

			List<ReportGroup> currentReportGroups = reportGroupService.getAllReportGroups();
			List<Role> currentRoles = roleService.getAllRoles();

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			Map<String, ReportGroup> addedReportGroups = new HashMap<>();
			Map<String, Role> addedRoles = new HashMap<>();
			List<Integer> updatedReportGroupIds = new ArrayList<>();
			List<Integer> updatedRoleIds = new ArrayList<>();

			for (UserGroup userGroup : userGroups) {
				ReportGroup defaultReportGroup = userGroup.getDefaultReportGroup();
				if (defaultReportGroup != null) {
					String reportGroupName = defaultReportGroup.getName();
					if (StringUtils.isBlank(reportGroupName)) {
						userGroup.setDefaultReportGroup(null);
					} else {
						ReportGroup existingReportGroup = currentReportGroups.stream()
								.filter(g -> StringUtils.equals(reportGroupName, g.getName()))
								.findFirst()
								.orElse(null);
						if (existingReportGroup == null) {
							ReportGroup addedReportGroup = addedReportGroups.get(reportGroupName);
							if (addedReportGroup == null) {
								reportGroupId++;
								reportGroupService.saveReportGroup(defaultReportGroup, reportGroupId, actionUser, conn);
								addedReportGroups.put(reportGroupName, defaultReportGroup);
							} else {
								userGroup.setDefaultReportGroup(addedReportGroup);
							}
						} else {
							if (overwrite) {
								int existingReportGroupId = existingReportGroup.getReportGroupId();
								if (!updatedReportGroupIds.contains(existingReportGroupId)) {
									defaultReportGroup.setReportGroupId(existingReportGroupId);
									reportGroupService.updateReportGroup(defaultReportGroup, actionUser, conn);
									updatedReportGroupIds.add(existingReportGroupId);
								}
							} else {
								userGroup.setDefaultReportGroup(existingReportGroup);
							}
						}
					}
				}

				List<Role> roles = userGroup.getRoles();
				if (CollectionUtils.isNotEmpty(roles)) {
					List<Role> newRoles = new ArrayList<>();
					for (Role role : roles) {
						String roleName = role.getName();
						Role existingRole = currentRoles.stream()
								.filter(r -> StringUtils.equals(roleName, r.getName()))
								.findFirst()
								.orElse(null);
						if (existingRole == null) {
							Role addedRole = addedRoles.get(roleName);
							if (addedRole == null) {
								roleId++;
								roleService.saveRole(role, roleId, actionUser, conn);
								addedRoles.put(roleName, role);
								newRoles.add(role);
							} else {
								newRoles.add(addedRole);
							}
						} else {
							if (overwrite) {
								int existingRoleId = existingRole.getRoleId();
								if (!updatedRoleIds.contains(existingRoleId)) {
									role.setRoleId(existingRoleId);
									roleService.updateRole(role, actionUser, conn);
									newRoles.add(role);
									updatedRoleIds.add(existingRoleId);
								}
							} else {
								newRoles.add(existingRole);
							}
						}
					}
					userGroup.setRoles(newRoles);
				}

				String userGroupName = userGroup.getName();
				boolean update = false;
				if (overwrite) {
					UserGroup existingUserGroup = currentUserGroups.stream()
							.filter(d -> StringUtils.equals(userGroupName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingUserGroup != null) {
						update = true;
						userGroup.setUserGroupId(existingUserGroup.getUserGroupId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					userGroupId++;
					newRecordId = userGroupId;
				}
				saveUserGroup(userGroup, newRecordId, actionUser, conn);
				userGroupPermissionService.recreateUserGroupPermissions(userGroup, conn);
				userGroupRoleService.recreateUserGroupRoles(userGroup, conn);
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves a user group
	 *
	 * @param group the user group to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveUserGroup(UserGroup group, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveUserGroup(group, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a user group
	 *
	 * @param group the user group
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public void saveUserGroup(UserGroup group, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveUserGroup: group={}, newRecordId={},"
				+ " actionUser={}", group, newRecordId, actionUser);

		//set values for possibly null property objects
		Integer defaultReportGroupId = null;
		if (group.getDefaultReportGroup() != null) {
			defaultReportGroupId = group.getDefaultReportGroup().getReportGroupId();
			if (defaultReportGroupId == 0) {
				defaultReportGroupId = null;
			}
		}

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_USER_GROUPS"
					+ " (USER_GROUP_ID, NAME, DESCRIPTION, DEFAULT_QUERY_GROUP,"
					+ " START_QUERY, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 7) + ")";

			Object[] values = {
				newRecordId,
				group.getName(),
				group.getDescription(),
				defaultReportGroupId,
				group.getStartReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(conn, sql, values);
		} else {
			String sql = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
					+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE USER_GROUP_ID=?";

			Object[] values = {
				group.getName(),
				group.getDescription(),
				defaultReportGroupId,
				group.getStartReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				group.getUserGroupId()
			};

			affectedRows = dbService.update(conn, sql, values);
		}

		if (newRecordId != null) {
			group.setUserGroupId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, group={}",
					affectedRows, newRecord, group);
		}
	}

	/**
	 * Returns users that are in a given user group
	 *
	 * @param userGroupId the user group id
	 * @return linked user names
	 * @throws SQLException
	 */
	public List<String> getLinkedUsers(int userGroupId) throws SQLException {
		logger.debug("Entering getLinkedUsers: userGroupId={}", userGroupId);

		String sql = "SELECT AU.USERNAME"
				+ " FROM ART_USERS AU"
				+ " INNER JOIN ART_USER_USERGROUP_MAP AUUGM"
				+ " ON AU.USER_ID=AUUGM.USER_ID"
				+ " WHERE AUUGM.USER_GROUP_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("USERNAME");
		return dbService.query(sql, h, userGroupId);
	}
	
	/**
	 * Returns <code>true</code> if a user group with the given name exists
	 *
	 * @param name the user group name
	 * @return <code>true</code> if a user group with the given name exists
	 * @throws SQLException
	 */
	@Cacheable(value = "userGroups")
	public boolean userGroupExists(String name) throws SQLException {
		logger.debug("Entering userGroupExists: name='{}'", name);

		String sql = "SELECT COUNT(*) FROM ART_USER_GROUPS"
				+ " WHERE NAME=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h, name);

		if (recordCount == null || recordCount.longValue() == 0) {
			return false;
		} else {
			return true;
		}
	}
}
