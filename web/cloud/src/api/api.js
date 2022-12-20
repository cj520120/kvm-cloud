import axios from "./request";
export const getNetworkList = (params = {}) => {
  return axios.request({
    url: "/api/network/all",
    params,
  });
};
export const getNetworkInfo = (params = {}) => {
  return axios.request({
    url: "/api/network/info",
    params,
  });
};
export const createNetwork = (data = {}) => {
  return axios.request({
    url: "api/network/create",
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
    url: "/api/network/register",
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

/** HOST */
export const getHostList = (params = {}) => {
  return axios.request({
    url: "/api/host/all",
    params,
  });
};
export const getHostInfo = (params = {}) => {
  return axios.request({
    url: "/api/host/info",
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
    url: "/api/host/register",
    data,
    method: "POST",
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
    url: "/api/storage/all",
    params,
  });
};
export const getStorageInfo = (params = {}) => {
  return axios.request({
    url: "/api/storage/info",
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
    url: "/api/storage/register",
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

/** Template */
export const getTemplateList = (params = {}) => {
  return axios.request({
    url: "/api/template/all",
    params,
  });
};
export const getTemplateInfo = (params = {}) => {
  return axios.request({
    url: "/api/template/info",
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
    url: "/api/snapshot/all",
    params,
  });
};
export const getSnapshotInfo = (params = {}) => {
  return axios.request({
    url: "/api/snapshot/info",
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

export const downloadSnapshot = (data = {}) => {
  return axios.request({
    url: "api/snapshot/download",
    data,
    method: "POST",
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
    url: "/api/volume/all",
    params,
  });
};
export const getVolumeInfo = (params = {}) => {
  return axios.request({
    url: "/api/volume/info",
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
/** Scheme */
export const getSchemeList = (params = {}) => {
  return axios.request({
    url: "/api/scheme/all",
    params,
  });
};
export const getSchemeInfo = (params = {}) => {
  return axios.request({
    url: "/api/scheme/info",
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
    url: "/api/guest/info",
    params,
  });
};
