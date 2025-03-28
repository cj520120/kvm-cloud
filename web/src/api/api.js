import axios from "./request";
export const getNetworkComponentList = (params = {}) => {
  return axios.request({
    url: "api/network/component",
    params,
  });
};
export const getNetworkList = (params = {}) => {
  return axios.request({
    url: "api/network/all",
    params,
  });
};
export const getNetworkInfo = (params = {}) => {
  return axios.request({
    url: "api/network/info",
    params,
  });
};
export const updateNetworkComponentSlaveNumber = (data = {}) => {
  return axios.request({
    url: "api/network/component/slave/update",
    data,
    method: "POST",
  });
};
export const createNetwork = (data = {}) => {
  return axios.request({
    url: "api/network/create",
    data,
    method: "PUT",
  });
};
export const createNetworkComponent = (data = {}) => {
  return axios.request({
    url: "api/network/component/create",
    data,
    method: "PUT",
  });
};
export const pauseNetwork = (data = {}) => {
  return axios.request({
    url: "api/network/maintenance",
    data,
    method: "POST",
  });
};

export const registerNetwork = (data = {}) => {
  return axios.request({
    url: "api/network/register",
    data,
    method: "POST",
  });
};

export const destroyNetwork = (data = {}) => {
  return axios.request({
    url: "api/network/destroy",
    data,
    method: "DELETE",
  });
};

export const getNetworkComponentNatList = (params = {}) => {
  return axios.request({
    url: "api/network/nat/list",
    params,
  });
};
export const createNetworkComponentNat = (data = {}) => {
  return axios.request({
    url: "api/network/nat/create",
    data,
    method: "PUT",
  });
};
export const destroyNetworkComponentNat = (data = {}) => {
  return axios.request({
    url: "api/network/nat/destroy",
    data,
    method: "DELETE",
  });
};
export const destroyNetworkComponent = (data = {}) => {
  return axios.request({
    url: "api/network/component/destroy",
    data,
    method: "DELETE",
  });
};

export const destroyNetworDns = (data = {}) => {
  return axios.request({
    url: "api/dns/destroy",
    data,
    method: "DELETE",
  });
};
/** HOST */
export const getHostList = (params = {}) => {
  return axios.request({
    url: "api/host/all",
    params,
  });
};
export const getHostInfo = (params = {}) => {
  return axios.request({
    url: "api/host/info",
    params,
  });
};
export const createHost = (data = {}) => {
  return axios.request({
    url: "api/host/create",
    data,
    method: "PUT",
  });
};

export const pauseHost = (data = {}) => {
  return axios.request({
    url: "api/host/maintenance",
    data,
    method: "POST",
  });
};

export const registerHost = (data = {}) => {
  return axios.request({
    url: "api/host/register",
    data,
    method: "POST",
  });
};

/** dns */
export const getNetworkDnsList = (params = {}) => {
  return axios.request({
    url: "api/dns/list",
    params,
  });
};
export const createNetworkDns = (data = {}) => {
  return axios.request({
    url: "api/dns/create",
    data,
    method: "PUT",
  });
};
export const destroyHost = (data = {}) => {
  return axios.request({
    url: "api/host/destroy",
    data,
    method: "DELETE",
  });
};

/** STORAGE */
export const getStorageList = (params = {}) => {
  return axios.request({
    url: "api/storage/all",
    params,
  });
};
export const getStorageInfo = (params = {}) => {
  return axios.request({
    url: "api/storage/info",
    params,
  });
};
export const createStorage = (data = {}) => {
  return axios.request({
    url: "api/storage/create",
    data,
    method: "PUT",
  });
};

export const pauseStorage = (data = {}) => {
  return axios.request({
    url: "api/storage/maintenance",
    data,
    method: "POST",
  });
};

export const registerStorage = (data = {}) => {
  return axios.request({
    url: "api/storage/register",
    data,
    method: "POST",
  });
};

export const migrateStorage = (data = {}) => {
  return axios.request({
    url: "api/storage/migrate",
    data,
    method: "POST",
  });
};
export const destroyStorage = (data = {}) => {
  return axios.request({
    url: "api/storage/destroy",
    data,
    method: "DELETE",
  });
};
export const clearStorage = (data = {}) => {
  return axios.request({
    url: "api/storage/clear",
    data,
    method: "DELETE",
  });
};
export const updateStorageSupportCategory = (data = {}) => {
  return axios.request({
    url: "api/storage/support/category/update",
    data,
    method: "POST",
  });
};

/** Template */
export const getTemplateList = (params = {}) => {
  return axios.request({
    url: "api/template/all",
    params,
  });
};
export const getTemplateInfo = (params = {}) => {
  return axios.request({
    url: "api/template/info",
    params,
  });
};
export const createTemplate = (data = {}) => {
  return axios.request({
    url: "api/template/create",
    data,
    method: "PUT",
  });
};
export const updateTemplateScript = (data = {}) => {
  return axios.request({
    url: "api/template/script",
    data,
    method: "POST",
  });
};

export const createVolumeTemplate = (data = {}) => {
  return axios.request({
    url: "api/template/volume/create",
    data,
    method: "PUT",
  });
};
export const downloadTemplate = (data = {}) => {
  return axios.request({
    url: "api/template/download",
    data,
    method: "POST",
  });
};

export const destroyTemplate = (data = {}) => {
  return axios.request({
    url: "api/template/destroy",
    data,
    method: "DELETE",
  });
};
/** Snapshot */
export const getSnapshotList = (params = {}) => {
  return axios.request({
    url: "api/snapshot/all",
    params,
  });
};
export const getSnapshotInfo = (params = {}) => {
  return axios.request({
    url: "api/snapshot/info",
    params,
  });
};
export const createSnapshot = (data = {}) => {
  return axios.request({
    url: "api/snapshot/create",
    data,
    method: "PUT",
  });
};

export const destroySnapshot = (data = {}) => {
  return axios.request({
    url: "api/snapshot/destroy",
    data,
    method: "DELETE",
  });
};

/** Volume */
export const getVolumeList = (params = {}) => {
  return axios.request({
    url: "api/volume/all",
    params,
  });
};
export const getNotAttachVolumeList = (params = {}) => {
  return axios.request({
    url: "api/volume/not/attach/all",
    params,
  });
};
export const getVolumeInfo = (params = {}) => {
  return axios.request({
    url: "api/volume/info",
    params,
  });
};
export const createVolume = (data = {}) => {
  return axios.request({
    url: "api/volume/create",
    data,
    method: "PUT",
  });
};

export const migrateVolume = (data = {}) => {
  return axios.request({
    url: "api/volume/migrate",
    data,
    method: "PUT",
  });
};
export const resizeVolume = (data = {}) => {
  return axios.request({
    url: "api/volume/resize",
    data,
    method: "POST",
  });
};
export const cloneVolume = (data = {}) => {
  return axios.request({
    url: "api/volume/clone",
    data,
    method: "PUT",
  });
};
export const destroyVolume = (data = {}) => {
  return axios.request({
    url: "api/volume/destroy",
    data,
    method: "DELETE",
  });
};
export const batchDestroyVolume = (data = {}) => {
  return axios.request({
    url: "api/volume/destroy/batch",
    data,
    method: "DELETE",
  });
};
/** Scheme */
export const getSchemeList = (params = {}) => {
  return axios.request({
    url: "api/scheme/all",
    params,
  });
};
export const getSchemeInfo = (params = {}) => {
  return axios.request({
    url: "api/scheme/info",
    params,
  });
};
export const createScheme = (data = {}) => {
  return axios.request({
    url: "api/scheme/create",
    data,
    method: "PUT",
  });
};

export const moidfyScheme = (data = {}) => {
  return axios.request({
    url: "api/scheme/modify",
    data,
    method: "POST",
  });
};

export const destroyScheme = (data = {}) => {
  return axios.request({
    url: "api/scheme/destroy",
    data,
    method: "DELETE",
  });
};
/** Guest */
export const getGuestInfo = (params = {}) => {
  return axios.request({
    url: "api/guest/info",
    params,
  });
};
export const getGuestVncPassword = (params = {}) => {
  return axios.request({
    url: "api/guest/vnc/password",
    params,
  });
};
export const getGuestVolumes = (params = {}) => {
  return axios.request({
    url: "api/guest/volume",
    params,
  });
};
export const getGuestNetworks = (params = {}) => {
  return axios.request({
    url: "api/guest/network",
    params,
  });
};
export const getGuestList = (params = {}) => {
  return axios.request({
    url: "api/guest/all",
    params,
  });
};
export const getSystemGuestList = (params = {}) => {
  return axios.request({
    url: "api/guest/system",
    params,
  });
};
export const getUserGuestList = (params = {}) => {
  return axios.request({
    url: "api/guest/user",
    params,
  });
};
export const destroyGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/destroy",
    data,
    method: "DELETE",
  });
};
export const createGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/create",
    data,
    method: "PUT",
  });
};
export const reInstallGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/reinstall",
    data,
    method: "POST",
  });
};
export const startGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/start",
    data,
    method: "POST",
  });
};
export const migrateGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/migrate",
    data,
    method: "POST",
  });
};
export const batchStartGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/start/batch",
    data,
    method: "POST",
  });
};
export const stopGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/shutdown",
    data,
    method: "POST",
  });
};
export const batchStoptGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/shutdown/batch",
    data,
    method: "POST",
  });
};
export const rebootGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/reboot",
    data,
    method: "POST",
  });
};
export const modifyGuest = (data = {}) => {
  return axios.request({
    url: "api/guest/modify",
    data,
    method: "POST",
  });
};
export const attachGuestCdRoom = (data = {}) => {
  return axios.request({
    url: "api/guest/cd/attach",
    data,
    method: "POST",
  });
};
export const detachGuestCdRoom = (data = {}) => {
  return axios.request({
    url: "api/guest/cd/detach",
    data,
    method: "POST",
  });
};

export const modifyAttachGuestDisk = (data = {}) => {
  return axios.request({
    url: "/api/guest/disk/device/modify",
    data,
    method: "POST",
  });
};
export const attachGuestDisk = (data = {}) => {
  return axios.request({
    url: "api/guest/disk/attach",
    data,
    method: "POST",
  });
};
export const detachGuestDisk = (data = {}) => {
  return axios.request({
    url: "api/guest/disk/detach",
    data,
    method: "POST",
  });
};
export const attachGuestNetwork = (data = {}) => {
  return axios.request({
    url: "api/guest/network/attach",
    data,
    method: "POST",
  });
};
export const detachGuestNetwork = (data = {}) => {
  return axios.request({
    url: "api/guest/network/detach",
    data,
    method: "POST",
  });
};
// User

export const oauth2Login = (data = {}) => {
  return axios.request({
    url: "api/user/oauth2/login",
    data,
    method: "POST",
  });
};
export const loginUser = (data = {}) => {
  return axios.request({
    url: "api/user/login",
    data,
    method: "POST",
  });
};
export const getUserList = (data = {}) => {
  return axios.request({
    url: "api/user/list",
    data,
    method: "GET",
  });
};
export const registerUser = (data = {}) => {
  return axios.request({
    url: "api/user/register",
    data,
    method: "PUT",
  });
};
export const updateUserState = (data = {}) => {
  return axios.request({
    url: "api/user/state/update",
    data,
    method: "POST",
  });
};

export const resetUserPassword = (data = {}) => {
  return axios.request({
    url: "api/user/password/reset",
    data,
    method: "POST",
  });
};

export const modifyUserPassword = (data = {}) => {
  return axios.request({
    url: "api/user/password/modify",
    data,
    method: "POST",
  });
};

export const destroyUser = (data = {}) => {
  return axios.request({
    url: "api/user/destroy",
    data,
    method: "DELETE",
  });
};

export const getLoginSignature = (params = {}) => {
  return axios.request({
    url: "api/user/login/signature",
    params,
  });
};
export const getCurrentLoginSignature = (params = {}) => {
  return axios.request({
    url: "api/user/signature",
    params,
  });
};
//config

export const getSystemConfig = (data = {}) => {
  return axios.request({
    url: "api/config",
    data,
    method: "GET",
  });
};

//group
export const getGroupList = (params = {}) => {
  return axios.request({
    url: "api/group/all",
    params,
  });
};
export const createGroup = (data = {}) => {
  return axios.request({
    url: "api/group/create",
    data,
    method: "PUT",
  });
};

export const modifyGroup = (data = {}) => {
  return axios.request({
    url: "api/group/update",
    data,
    method: "POST",
  });
};

export const getGroupInfo = (params = {}) => {
  return axios.request({
    url: "api/group/info",
    params,
  });
};
export const destroyGroup = (data = {}) => {
  return axios.request({
    url: "api/group/destroy",
    data,
    method: "DELETE",
  });
};

//ssh
export const getSshList = (params = {}) => {
  return axios.request({
    url: "api/ssh/all",
    params,
  });
};
export const importSSh = (data = {}) => {
  return axios.request({
    url: "api/ssh/import",
    data,
    method: "PUT",
  });
};

export const createSSh = (data = {}) => {
  return axios.request({
    url: "api/ssh/create",
    data,
    method: "PUT",
  });
};
export const getSshInfo = (params = {}) => {
  return axios.request({
    url: "api/ssh/info",
    params,
  });
};
export const destroySsh = (data = {}) => {
  return axios.request({
    url: "api/ssh/destroy",
    data,
    method: "DELETE",
  });
};
export const createSshDownloadKey = (data = {}) => {
  return axios.request({
    url: "/api/ssh/download/key",
    data,
    method: "POST",
  });
};
//config
export const getConfigList = (params = {}) => {
  return axios.request({
    url: "api/config/search",
    params,
  });
};
export const createConfig = (data = {}) => {
  return axios.request({
    url: "api/config/create",
    data,
    method: "PUT",
  });
};

export const modifyConfig = (data = {}) => {
  return axios.request({
    url: "api/config/update",
    data,
    method: "POST",
  });
};
export const destroyConfig = (data = {}) => {
  return axios.request({
    url: "api/config/destroy",
    data,
    method: "DELETE",
  });
};
