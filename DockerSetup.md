# Set up Core
## IPFS 
	https://blog.ipfs.io/1-run-ipfs-on-docker/
	
	docker pull jbenet/go-ipfs:latest 
	
	mkdir -p /tmp/ipfs-docker-staging 	
	
	mkdir -p /tmp/ipfs-docker-data
	
	docker run -d --name ipfs-node -v /tmp/ipfs-docker-staging:/export -v /tmp/ipfs-docker-data:/data/ipfs -p 4001:4001 -p 5001:5001 jbenet/go-ipfs:latest 
	{a8311daa0f8d3c733ea4dacb813eba959662b42a2d6b3d876d27de2bfd223a92}
	cid=a8311daa0f8d3c733ea4dacb813eba959662b42a2d6b3d876d27de2bfd223a92
	
	docker ps
	CONTAINER ID        IMAGE                   COMMAND                  CREATED             STATUS              PORTS                                                           NAMES
	a8311daa0f8d        jbenet/go-ipfs:latest   "/sbin/tini -- /usr/…"   32 seconds ago      Up 30 seconds       0.0.0.0:4001->4001/tcp, 0.0.0.0:5001->5001/tcp, 8080-8081/tcp   ipfs-node

	docker exec a8311daa0f8d ipfs id
	{
			"ID": "QmYzrGqAznuSoritgE22gkVRdgy6Lwp2b5JLYwBc4Noboz",
			"PublicKey": "CAASpgIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCz+gYpQtyOSvqVoFUbD6BJhePKhB1lXCXWSi2yyfPI9bRNaRci5l8NtAxxK2gdsDLhESrlROTleX4o7LrSVNhpRWeH0lLIGGKB04LI96nh6iEcR0vnER5UnDCBmFIhSZQla86ou6vgt0DnsXxZ86QX8Lew7upLt0SvCQgg1BtTwuORoWk/8bAWBIYA+K3AzJr7BZ7I7ZTomeTh76hE+NAXpkHPB/+7ePXbnVhuzkU1J/03ktWOy7AlhgkGi52I5YaewtJZYSjFLGofnQvJZZrXlW2Rm9kJj58LQgMh1X+r6cC+lL0aLkVZjK6HGMKOOL3ARw3CkWQNHfCd2dXYQq5XAgMBAAE=",
			"Addresses": [
					"/ip4/127.0.0.1/tcp/4001/ipfs/QmYzrGqAznuSoritgE22gkVRdgy6Lwp2b5JLYwBc4Noboz",
					"/ip4/172.17.0.2/tcp/4001/ipfs/QmYzrGqAznuSoritgE22gkVRdgy6Lwp2b5JLYwBc4Noboz",
					"/ip4/101.96.120.41/tcp/4001/ipfs/QmYzrGqAznuSoritgE22gkVRdgy6Lwp2b5JLYwBc4Noboz"
			],
			"AgentVersion": "go-ipfs/0.4.21/",
			"ProtocolVersion": "ipfs/0.1.0"
	}
	
	docker exec a8311daa0f8d ipfs swarm peers   
	
	echo "hello from dockerized ipfs" >/tmp/ipfs-docker-staging/hello
	
	docker exec a8311daa0f8d3c733ea4dacb813eba959662b42a2d6b3d876d27de2bfd223a92 ipfs add /export/hello
	added QmcDge1SrsTBU8b9PBGTGYguNRnm84Kvg8axfGURxqZpR1 hello
	
	docker exec a8311daa0f8d3c733ea4dacb813eba959662b42a2d6b3d876d27de2bfd223a92 ipfs cat /ipfs/QmcDge1SrsTBU8b9PBGTGYguNRnm84Kvg8axfGURxqZpR1
	
	curl http://localhost:5001/ipfs/QmcDge1SrsTBU8b9PBGTGYguNRnm84Kvg8axfGURxqZpR1
	
## PostgreSQL	
	https://dev.to/shree_j/how-to-install-and-run-psql-using-docker-41j2
	
	docker run --name postgresql-container -p 5432:5432 -e POSTGRES_PASSWORD=postgres -d postgres	

	docker ps
	
	docker exec -it f2c543f8ee1d bash
	
	psql -h localhost -p 5432 -U postgres -W
	
	Set up Database:
	
	CREATE DATABASE comlake;	
	
	\c comlake
	
	DROP TABLE IF EXISTS content CASCADE;
	
	CREATE TABLE content (cid text PRIMARY KEY, type text, extra jsonb);
	
	DROP TABLE IF EXISTS dataset;
	
	CREATE TABLE dataset (id bigserial PRIMARY KEY, file text REFERENCES content, description text, source text, topics text[], extra jsonb, parent bigint, FOREIGN KEY (parent) REFERENCES dataset(id));
	
	\dt
	
# Deploy Jar File
	We're not going to install lein, it's too much of a hassle;
	
	## Downloading the jar file instead
	
	## Create a Dockerfile: 
	
	FROM adoptopenjdk/openjdk11:latest
	VOLUME /tmp
	EXPOSE 8090
	ADD comlake.core-0.4.0-standalone.jar comlake.core-0.4.0-standalone.jar
	ENTRYPOINT ["java","-jar","comlake.core-0.4.0-standalone.jar"]
	
	## Build: 
	
	docker image build -t comlake-core .

	## Create a bridge network from postgresql to comlake-core: 
	
	docker network create core-postgres
	
	docker network connect core-postgres postgresql-container
	
	## Run comlake-core and link to postgres: 
	
	docker container run --network core-ipfs --name comlake-core-container -p 8090:8090 -d comlake-core

	5a67be8ce1d5f203988279a3af4fafb0fd3bc69d65d9d92e437cdf657c0c514e
	
	Check if there is any bug: 
	
	docker container logs -f 5a67be8ce1d5f203988279a3af4fafb0fd3bc69d65d9d92e437cdf657c0c514e
	
	docker network create core-ipfs
	
	docker network connect core-ipfs ipfs-node
	
	docker network connect core-ipfs comlake-core-container
	
	docker run -d --network core-ipfs --name ipfs-node \
	-v /tmp/ipfs-docker-staging:/export -v /tmp/ipfs-docker-data:/data/ipfs \
	-p 8080:8080 -p 4001:4001 -p 127.0.0.1:5001:5001 \
	ipfs/go-ipfs:latest

	c54d217f877de4cb132263bb061b0bc9e57cb7890831aadfebc2850848e76430

