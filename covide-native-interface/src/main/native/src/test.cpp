/*
 * This file is part of the CoVIda project.
 * Copyright (C) 2013 DFKI GmbH. All rights reserved.
 *
 * Disclaimer:
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND
 * CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
#include "CovidaNativeInterface.h"
#include "algorithm.h"
using namespace std;



int main()
{
	// add the image you want to test here 
	const string image_path = "D:\\data\\plant-image-ayumi\\BY2\\IMAGE_4_0002.tif";

	const string description = "annotation_string";
	onImageOpened(image_path);
	vector<Point*> vpoints;
	// add useful points like this... for testing
	vpoints.push_back(new Point(34.f, 34.f));
	vpoints.push_back(new Point(100.f, 100.f));
	lineAnnotation(image_path, description, 0L, 0L, 34.f, 34.f, 100.f, 100.f, vpoints);

	onImageClosed(image_path);
    return 0;
}