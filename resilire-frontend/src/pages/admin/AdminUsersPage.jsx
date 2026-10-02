import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as adminApi from "../../api/adminApi";

export default function AdminUsersPage() {
  const { t } = useTranslation();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");

  const loadUsers = () => {
    adminApi
      .listUsers()
      .then((page) => setUsers(page.content))
      .catch(() => setError(t("admin.errorFallbackLoad")));
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleToggle = async (user) => {
    setError("");
    try {
      if (user.enabled) {
        await adminApi.disableUser(user.id);
      } else {
        await adminApi.enableUser(user.id);
      }
      loadUsers();
    } catch (err) {
      setError(err.response?.data?.message || t("admin.errorFallbackUpdate"));
    }
  };

  return (
    <div className="form-page wide">
      <h1>{t("admin.title")}</h1>
      {error && <p className="form-error">{error}</p>}
      <table className="data-table">
        <thead>
          <tr>
            <th>{t("admin.email")}</th>
            <th>{t("admin.role")}</th>
            <th>{t("admin.status")}</th>
            <th>{t("admin.created")}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{user.email}</td>
              <td>{user.role}</td>
              <td>{user.enabled ? t("common.userStatus.enabled") : t("common.userStatus.disabled")}</td>
              <td>{new Date(user.createdAt).toLocaleDateString()}</td>
              <td>
                <button className="link-button" onClick={() => handleToggle(user)}>
                  {user.enabled ? t("admin.disable") : t("admin.enable")}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
