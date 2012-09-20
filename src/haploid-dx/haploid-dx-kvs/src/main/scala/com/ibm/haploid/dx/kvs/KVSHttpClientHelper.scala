package com.ibm.haploid

package dx

package kvs

import org.apache.http.auth.{UsernamePasswordCredentials, AuthScope}
import org.apache.http.client.params.HttpClientParams
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.HttpParams
import org.restlet.Client

class KVSHttpClientHelper(client: Client) extends org.restlet.ext.httpclient.HttpClientHelper(client) {

  override def configure(httpClient: DefaultHttpClient) {
    super.configure(httpClient)

    val proxyHost = this.getProxyHost()
    if (proxyHost != null) {
      val proxyPort = getProxyPort()

      this.getHttpClient.asInstanceOf[DefaultHttpClient].getCredentialsProvider().setCredentials(
        new AuthScope(proxyHost, proxyPort),
        new UsernamePasswordCredentials(com.ibm.haploid.dx.proxyUser, com.ibm.haploid.dx.proxyPassword))
    }
  }

  override def configure(params: HttpParams) {
    super.configure(params)
    HttpClientParams.setAuthenticating(params, true)
  }

}