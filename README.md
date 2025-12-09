# Setup for running application stack onto minikube cluster

All commands below assume the user is on the root directory

## Step-by-step

### Deployment Steps

#### (1) Deploy Postgresql

1. (Optional) Render & validate postgresql: `helm template minikube-setup/postgresql -f values.yml`
2. Create namespace: `kubectl create ns postgres`
3. Deploy postgres into minikube: `helm upgrade --install postgres minikube-setup/postgresql -f minikube-setup/values.yml --namespace postgres`

#### (2) Create database + users

1. Run the setup SQL file onto the postgres pod: `kubectl exec -i postgres-0 -n postgres -- psql -U postgres -d urls < ./minikube-setup/postgresql/setup/create-users.sql`

#### (3) Prepare secrets for services

1. Create secret for url-shortener
```bash
kubectl create secret generic url-shortener-service-db-secret \
  --from-literal=username=url_shortener_user \
  --from-literal=password=url_shortener_secure_password \
  --namespace dev
```
2. Create secret for redirect-service
```bash
kubectl create secret generic redirect-service-db-secret \
  --from-literal=username=redirect_user \
  --from-literal=password=redirect_secure_password \
  --namespace dev
```

#### (4) Build container images

1. `docker build -t url-shortener:latest url-shortener-service/`

2. `docker build -t redirect-service:latest redirect-service/`

3. `minikube image load url-shortener:latest`

#### (5) Deploy Istio

1. Install gateway CRDs
```
kubectl get crd gateways.gateway.networking.k8s.io &> /dev/null || \
{ kubectl kustomize "github.com/kubernetes-sigs/gateway-api/config/crd?ref=v1.4.0" | kubectl apply -f -; }
```

2. Install Istio on the cluster
```
istioctl install --set profile=minimal -y
```

#### (5) Deploy url-shortener-service

1. If not created - `kubectl create namespace dev`
2. `helm upgrade --install url-shortener-service url-shortener-service/helm -f minikube-setup/values.yml --namespace dev`

#### (6) Deploy redis

1. `kubectl create namespace redis`
2. `helm upgrade --install redis minikube-setup/redis -f minikube-setup/values.yml --namespace redis`

#### (7) Deploy redirect-service 

1. `helm upgrade --install redirect-service redirect-service/helm -f minikube-setup/values.yml --namespace dev`

#### (8) Install Nginx Ingress

1. `helm upgrade --install ingress-nginx minikube-setup/ingress-nginx -f minikube-setup/values.yml --namespace ingress-nginx`

#### (9) Install ELK Stack for monitoring

1. Deploy Elasticsearch: `helm upgrade --install elasticsearch minikube-setup/elasticsearch --namespace elk -f minikube-setup/values.yml`
2. Check Elasticsearch pods are healthy
3. Deploy Logstash: `helm upgrade --install logstash minikube-setup/logstash --namespace elk -f minikube-setup/values.yml`
4. Check logstash is healthy
5. Deploy Kibana: `helm upgrade --install kibana minikube-setup/kibana --namespace elk -f minikube-setup/values.yml`
6. Check Kibana is healthy
7. Deploy Filebeat: `helm upgrade --install filebeat minikube-setup/filebeat --namespace elk -f minikube-setup/values.yml`