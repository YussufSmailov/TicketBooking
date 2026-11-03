const API_URL = "http://localhost:8080/api";

async function request(url, method, body) {
  const options = {
    method: method,
    headers: { "Content-Type": "application/json" }
  };
  if (body) {
    options.body = JSON.stringify(body);
  }

  const response = await fetch(API_URL + url, options);

  if (!response.ok) {
    let message = "Ошибка сервера";
    try {
      const data = await response.json();
      if (data.message) {
        message = data.message;
      }
    } catch {
      message = "Ошибка сервера";
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export function getData(url) {
  return request(url, "GET");
}

export function postData(url, body) {
  return request(url, "POST", body);
}

export function putData(url, body) {
  return request(url, "PUT", body);
}

export function deleteData(url) {
  return request(url, "DELETE");
}
