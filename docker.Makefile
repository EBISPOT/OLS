
# Building docker image
VERSION = "0.0.1"
PRODUCT=ols-web
IM=ihcc/$(PRODUCT)

docker-build-no-cache:
	@docker build --no-cache -t $(IM):$(VERSION) -f ./$(PRODUCT)/Dockerfile . \
	&& docker tag $(IM):$(VERSION) $(IM):latest
	
docker-build:
	@docker build -t $(IM):$(VERSION) -f ./$(PRODUCT)/Dockerfile . \
	&& docker tag $(IM):$(VERSION) $(IM):latest

docker-run: docker-build
	docker run -p 8080:90 $(IM)

docker-clean:
	docker kill $(IM) || echo not running ;
	docker rm $(IM) || echo not made 

docker-publish-no-build:
	@docker push $(IM):$(VERSION) \
	&& docker push $(IM):latest
	
docker-publish: docker-build
	@docker push $(IM):$(VERSION) \
	&& docker push $(IM):latest
	
#include dumps.Makefile