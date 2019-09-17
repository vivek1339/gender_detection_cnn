from keras.models import load_model
import cv2
import numpy as np
from keras.optimizers import SGD
model = load_model('Model.h5')
model.compile(SGD(momentum=.9, nesterov=True), 'binary_crossentropy', metrics=['accuracy'])
from keras.preprocessing import image
while(True):
	test_image = image.load_img(input("enter image name:")+'.jpg', target_size = (100, 100)) 
	test_image = image.img_to_array(test_image)
	test_image = np.expand_dims(test_image, axis = 0)
	#print(test_image[0][0][0])
	test_image=test_image/127.5 -1
	#predict the result
	result = model.predict(test_image)
	if(result>0.5):
		print("Given image is of female. And predicted with an confidence of ",result*100,"%")
	else:
		print("Given image is of male. And predicted with an confidence of ",(1-result)*100,"%")