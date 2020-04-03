VERSION = "3.3.1" 
IM=ebispot/ols

docker-build:
	@docker build -t $(IM):$(VERSION) . \
	&& docker tag $(IM):$(VERSION) $(IM):latest

docker-publish: docker-build
	@docker push $(IM):$(VERSION) \
	&& docker push $(IM):latest
	
docker-build-no-cache:
	@docker build --no-cache -t $(IM):$(VERSION) . \
	&& docker tag $(IM):$(VERSION) $(IM):latest