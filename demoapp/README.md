# Using this as a starter pack

Helm has "starter packs" to make creating charts a bit simpler. To set this up
you will need to checkout this repo and then run `helm init --client-only &&
cd kubernetes-deployment && ln -s $PWD/helm/sampleapp
~/.helm/starters/mns-sampleapp`. You can then run `helm create -p
mns-sampleapp helm/` in your own app and it will write most of
these files you need automatically

# Pull an Image from a Private Registry

kubectl create secret docker-registry $NAME --docker-server=$DOCKER-SERVER --docker-username=***** --docker-password=***** --docker-email=DL-DevOps-CISupport@marks-and-spencer.com

kubectl patch serviceaccount default --namespace=$NAMESPACE -p '{"imagePullSecrets": [{"name": "$NAME"}]}'

## First Time Setup For Chart Creator.

If you have just created this sample chart with `helm create -p mns-sampleapp`
you will need to edit a few lines in values.yaml, then remove this section
from the readme:

- `dockerImage` will need to be set to what ever image your CI pipeline is
  building.
- `ingress.name`, `ingress.domain`, `ingress.subdomain`, `ingress.cluster` will need to be set so as to create a cluster-wide unique name. Currently there is no enforcing of uniqueness so if you overlap with another team you will steal their traffic. Don't do this :) Ask in #paas-feedback to make sure the name you pick is unique. To know more about how these 4 keys create ingress domain, read [below](#configuration) and [further below](#creating-your-ingress-domain)
- `resources` key sets the memory and cpu limits for your containers. You will
  need to work out what sensible values for your app is and set them.
- `namespace` key sets the Kubernetes namespace to which components will be deployed. Namespaces can be used to logically group components in your helm chart. Namespaces can also be used to restrict the CPU and memory used by pods, but usual defaults are fine.

**Remember** Now edit README.md and delete up to this line.

# Helm deployment chart for Our Sample App.

## Prerequisites

- Access to a Kubernetes cluster, 1.5+, with Helm and an ingress controller
  installed. (The M&S clusters have these. IF you are using minikube etc. it's
  up to you.)
- You must provide one of `dockerSha` or `dockerTag` values - the concourse
  pipeline takes care of this.
- You must provide a value for `ingress.domain`. The value to use here depends
  on which cluster you are deploying to. Again the concourse pipeline will
  provide this for you.

## Configuration

NOTE: See [below](#creating-your-ingress-domain) for details on how to construct your ingress domain via keys in `values.yaml`.

The following tables lists the configurable parameters of the Prometheus chart and their default values.

| Parameter | Description | Default |
| --------- | ----------- | ------- |
| `dockerImage` | Name ofthe docker image to deploy | `registry.pulp.devops.mnscorp.net/platform-tech/k8s-auth-helper` |
| `dockerSha` | Content-addressable SHA of docker image to deploy | N/A - set at deploy time |
| `dockerTag` | Tag name of docker image to deploy | N/A - set at deploy time |
| `resources` | resource requests and limits (YAML) |`requests: {cpu: 10m, memory: 64Mi}` |
| `ingress.name` | The subdomain component to request the ingress route for. | `sampleapp` |
| `ingress.domain` | The domainname to request the ingress route for. Overriden by combination of `ingress.subdomain` and `ingress.cluster`. | N/A - set at deployment time |
| `ingress.subdomain` | The subdomain used to create ingress. Valid options are `apps`, `platform`, `services`. Must define `ingress.cluster` too. Overrides `ingress.domain`. | `platform` |
| `ingress.cluster` | The cluster this application is being deployed to. | N/A - This needs to be filled before deployment |
| `ingress.access_policy` | Which ingress class to use. Choices are `public` (A.K.A. open to everywhere), `mns-only` (IP restricted to M&S IP ranges), `cdn-only` to only allow access from the CDN or "authorized" origins. | `mns-only` |
| `ingress.ssl` | Should the ingress route use HTTPS? | `false` |
| `namespace` | Kubernetes namespace to which the components will be deployed | `default` |

### Creating your ingress domain

The ingress domain of any service being hosted on a Kubernetes cluster consists of many parts. An example ingress domain could be:
```
sampleapp.dev.platform.mnscorp.net
```
Parts of this domain are:  
+ `sampleapp`: The service/application name. Provided by `ingress.name` key in `values.yaml`.
+ `dev`: The cluster name where this application will be deployed. Provided by `ingress.cluster` key.
+ `platform`: The type of service. The available options are `apps`, `platform` and `services`. Provided by `ingress.subdomain` key.
+ `mnscorp.net`: The base domain for everything hosted on Kubernetes. This is always constant.

***NOTE***: Apart from these values, we also have `ingress.domain`, which has been deprecated but still kept for posterity. Although you can create your ingress using this key, but support for this key will be removed sometime in future.  
***NOTE2***: `ingress.subdomain` and `ingress.cluster` have precedence over `ingress.domain`. If you specify `ingress.subdomain` and `ingress.cluster` in `values.yaml`, `ingress.domain` value will be ignored.

### Removing stack

helm delete $NAME --purge --tiller-namespace=$TILLER_NAME
 
